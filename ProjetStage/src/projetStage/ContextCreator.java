package projetStage;

import java.io.*;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.vividsolutions.jts.geom.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;

import projetStage.agents.building.*;
import projetStage.agents.controller.EnergyManager;
import projetStage.agents.controller.Meteo;
import projetStage.agents.building.Microgrid;

/**
 * ContextCreator est la classe qui permet d'initialiser la simulation. La fonction build(Context<Object> context)
 * est appellée lorsque l'utilisateur clique sur le bouton 'Initialize run', dans la fenêtre de Repast
 */
public class ContextCreator implements ContextBuilder<Object> {
    private static final SimpleDateFormat universalFullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // Paramètres de la simulation rentrés par l'utilisateur
    private static final int NB_BUILDING_MIN = 1;
    private static int NB_BUILDING_MAX;
    private static double DISTANCE_MAX;
    private static double GRID_DIMENSION;
    private Calendar BEGIN_DATE;

    // Coordonnées de l'île de la Réunion multipliées par 1000
    private static final double LONG_MIN = 55215;   // Longitude minimale
    private static final double LONG_MAX = 55837;   // Longitude maximale
    private static final double LAT_MIN = -21389;   // Latitude minimale
    private static final double LAT_MAX = -20871;   // Latitude maximale

    /* Structure snécessaires pour travailler avec une grilles qui permet au final de créer
     * les microgrids beaucoup plus rapidement. La taille des cases de la grille est définie en
     * fonction du rayon max des microgrid défini par l'utilisateur.
     * Dans les 3 maps suivantes, Coordinate représente le coin supérieur gauche d'une case de la grille
     */
    // Map contenant la liste des bâtiments contenus dans chaque cases de la grille
    private final Map<Coordinate, List<Building>> buildingMap = new HashMap<>();
    // Map contenant la liste des microgrid contenues dans chaque cases de la grille
    private final Map<Coordinate, List<Microgrid>> microgridMap = new HashMap<>();
    // Map contenant les cases de la grilles avec en clé, la distance qui les sépare du centre de l'ile
    // Le but est ensuite de pouvoir travailler avec les cases de la grille en partant de l'exterieur de l'ile
    private final TreeMap<Double, List<Coordinate>> keyMap = new TreeMap<>();

    private EnergyManager energyManager;    // EnergyManager


    /**
     * Construction et initialisation de la simulation
     * - Lectures des variables de la simulation
     * - Construction des grilles
     * - Chargement des batiments selectionnés
     * - Création et initialisation de la l'agent responsable de la récupération des données du DWH (Météo)
     * - Initialisation des producteurs d'énergie
     * - Création des microgrids
     *
     * @param context context
     *
     * @return context
     */
    public Context<Object> build(Context<Object> context) {
        System.out.println("****** Initialisation ******");

        //Initialisation Geography
        final GeographyParameters<Object> geoParams = new GeographyParameters<>();
        final Geography<Object> geography = GeographyFactoryFinder.createGeographyFactory(null).createGeography("Geography", context, geoParams);

        // Début du timer
        long time = System.currentTimeMillis();

        // Lecture des paramètres, création de Meteo, EnergyManager
        readParameters(context, geography);

        // Création des microgrids
        buildMicrogrid(context, geography);

        // Affichage du timer
        System.out.println("Time : " + (System.currentTimeMillis() - time));

        return context;
    }


    /**
     * - Lecture des parametres
     * - Initialisatin des grilles
     * - Création des agents EnergyManager et Meteo
     * - Chargement des batiments
     *
     * @param context   context
     * @param geography geography
     */
    public void readParameters(Context<Object> context, Geography<Object> geography) {
        Parameters params = RunEnvironment.getInstance().getParameters();
        List<String> types = new ArrayList<>();
        List<String> cities = new ArrayList<>();

        /************************************** PARAMETRES ****************************************/

        // Initialisation des variables globales
        NB_BUILDING_MAX = params.getInteger("NB_BUILDING_MAX");
        DISTANCE_MAX = params.getDouble("DISTANCE_MAX");
        GRID_DIMENSION = DISTANCE_MAX * 2 * 1000;

        /**************************************** GRILLES *****************************************/

        // Création des grilles
        Map<Coordinate, List<String>> producerMap = new HashMap<>();
        for (double x = LONG_MIN; x < LONG_MAX; x += GRID_DIMENSION) {
            for (double y = LAT_MIN; y < LAT_MAX; y += GRID_DIMENSION) {
                Coordinate coord = new Coordinate(x, y);
                buildingMap.put(coord, new ArrayList<Building>());
                microgridMap.put(coord, new ArrayList<Microgrid>());
                producerMap.put(coord, new ArrayList<String>());
            }
        }

        /************************************* ENERGYMANAGER **************************************/

        // Création de l'agent EnergyManager et des producteurs d'énergie
        energyManager = new EnergyManager(producerMap);
        energyManager.createProducers(context, geography);
        energyManager.initProducerMap();
        context.add(energyManager);

        /***************************************** METEO ******************************************/

        // Récupération et initialisation de la date
        BEGIN_DATE = Calendar.getInstance();
        try {
            BEGIN_DATE.setTime(universalFullDateFormat.parse(params.getString("BEGIN_DATE")));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Création de l'agent Meteo
        boolean m = params.getBoolean("Meteo");
        if (m) {
            Meteo meteo = new Meteo(BEGIN_DATE);
            context.add(meteo);
        }

        /*************************************** BATIMENTS ****************************************/

        // Chargement du profil de consommation
        Map<String, Double> mapConso = loadConso();

        // Lecture des batiments à charger
        if (params.getBoolean("Batiments industriels")) types.add("industriel");
        if (params.getBoolean("Batiments remarquables")) types.add("remarquable");
        if (params.getBoolean("Batiments indifferencies")) types.add("indifferencie");
        if (params.getBoolean("Batiments sportifs")) types.add("sport");
        if (params.getBoolean("St-Andre")) cities.add("st-Andre");
        if (params.getBoolean("St-Benoit")) cities.add("st-Benoit");
        if (params.getBoolean("St-Denis")) cities.add("st-Denis");
        if (params.getBoolean("St-Leu")) cities.add("st-Leu");
        if (params.getBoolean("St-Louis")) cities.add("st-Louis");
        if (params.getBoolean("St-Paul")) cities.add("st-Paul");
        if (params.getBoolean("St-Pierre")) cities.add("st-Pierre");
        if (params.getBoolean("St-Joseph")) cities.add("no-mans-land");

        // Chargement des batiments
        for (String type : types) {
            for (String ville : cities) {
                loadFeatures("./data/buildings/" + ville + "/" + type + "/building-" + type + "-" + ville + ".shp", mapConso);
            }
        }

        // Récupération des cases de la grille non vide et stockage en fonction de leur distance au centre de la carte
        Coordinate center = new Coordinate((LONG_MAX + LONG_MIN) / 2, (LAT_MAX + LAT_MIN) / 2);
        for (Map.Entry<Coordinate, List<Building>> entry : buildingMap.entrySet()) {
            if (entry.getValue().size() > 0) {
                double dist = -entry.getKey().distance(center);
                if (!keyMap.containsKey(dist)) {
                    keyMap.put(dist, new ArrayList<Coordinate>());
                }
                keyMap.get(dist).add(entry.getKey());
            }
        }
    }


    /**
     * Chargement des batiments à partir de fichiers shapefiles
     *
     * @param filename Nom du fichier a charger
     * @param mapConso Profil de consommation des batiments
     */
    private void loadFeatures(final String filename, final Map<String, Double> mapConso) {
        System.out.println("-> Loading " + filename);
        try {
            URL url = new File(filename).toURL();
            ShapefileDataStore store = new ShapefileDataStore(url);
            SimpleFeatureIterator fiter = store.getFeatureSource().getFeatures().features();
            while (fiter.hasNext()) {
                Building building = null;
                SimpleFeature feat = fiter.next();
                final String id = (String)feat.getAttribute("ID");
                final Geometry geom = ((Geometry)feat.getDefaultGeometry());
                String nature;
                if (filename.contains("indifferencie")) {
                    nature = (String)feat.getAttribute("ORIGIN_BAT");
                    building = new UndifferentiatedBuilding(id, nature, geom);
                } else {
                    nature = (String)feat.getAttribute("NATURE");
                    if (filename.contains("industriel"))
                        building = new IndustrialBuilding(id, nature, geom);
                    if (filename.contains("remarquable"))
                        building = new NoticeableBuilding(id, nature, geom);
                    if (filename.contains("sport"))
                        building = new SportBuilding(id, nature, geom);
                }
                building.setMapConso(mapConso);
                buildingMap.get(hashForGrid(geom.getCentroid().getCoordinate())).add(building);
            }

            fiter.close();
            store.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Chargement du profil de consommation des batiments
     *
     * @return une map contenant la consommation électrique d'un batiment pour chaque minutes
     */
    private Map<String, Double> loadConso() {
        Map<String, Double> map = new HashMap<>();

        try {
            InputStream ips = new FileInputStream("./data/conso/profil.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(ips));
            String ligne;
            while ((ligne = br.readLine()) != null) {
                String[] l = ligne.split(";");
                map.put(l[0], Double.parseDouble(l[1]));
            }
            br.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return map;
    }


    /**
     * Fonction permettant de construire les microgrids en fonction des paramètres entrés par
     * l'utilisateur et de la position des batiments
     *
     * @param context   Context
     * @param geography Geography
     */
    public void buildMicrogrid(final Context<Object> context, final Geography<Object> geography) {
        System.out.println("****** Microgrids' creation ******");
        final List<Building> buildingList = new ArrayList<>(); //
        final List<Building> losts = new ArrayList<>();
        int compteur = 0;

        // Pour chaque cases de la grille, en partant de l'exterieur de l'ile
        for (Map.Entry<Double, List<Coordinate>> entry : keyMap.entrySet()) {
            for (Coordinate coord : entry.getValue()) {
                // Récupération des batiments de la case actuelle
                List<Building> actualGridBuildingList = buildingMap.get(coord);

                if (!actualGridBuildingList.isEmpty()) {
                    // Recuperation des batiments des cases voisines sur la grille
                    List<Building> neighborhoodList = loadNeighborhood(coord, buildingMap);
                    neighborhoodList.addAll(actualGridBuildingList);

                    while (!actualGridBuildingList.isEmpty()) { // Tant que tous les batiments n'ont pas été traités
                        // On prend un batiment "au hasard" (le premier de la liste)
                        Coordinate center = actualGridBuildingList.get(0).getGeometry().getCentroid().getCoordinate();
                        buildingList.add(actualGridBuildingList.get(0));
                        buildingList.get(0).setDistance(0);
                        neighborhoodList.remove(actualGridBuildingList.get(0));

                        // On prend tous les batiments qui sont à une distance inférieure à DISTANCE_MAX
                        for (Building building : neighborhoodList) {
                            building.setDistance(building.getGeometry().getCoordinate().distance(center));
                            if (building.distance() < DISTANCE_MAX)
                                buildingList.add(building);
                        }

                        // Si il y a trop de batiments, on supprime les plus éloignés
                        if (buildingList.size() > NB_BUILDING_MAX) {
                            Collections.sort(buildingList);
                            Collections.reverse(buildingList);
                            for (int i = buildingList.size(); i > NB_BUILDING_MAX; --i) {
                                buildingList.remove(buildingList.get(i - 1));
                            }
                        }

                        // Traitement des batiments selectionnés pour constituer une microgrid ou les laisser à l'écart
                        if (buildingList.size() <= NB_BUILDING_MIN) {
                            // On stocke les batiments les plus isolés dans une liste pour les retraiter par la suite
                            losts.addAll(buildingList);
                        } else {
                            // On crée la microgrid avec les batiments selectionnés
                            compteur += buildingList.size();
                            Microgrid microgrid = new Microgrid(context, geography, energyManager, BEGIN_DATE, buildingList);
                            microgridMap.get(hashForGrid(microgrid.getCentroid())).add(microgrid);
                            context.add(microgrid);
                        }

                        // On supprime les batiments selectionnés de la liste des features
                        actualGridBuildingList.removeAll(buildingList);
                        neighborhoodList.removeAll(buildingList);
                        for (Building building : buildingList) {
                            buildingMap.get(hashForGrid(building.getGeometry().getCentroid().getCoordinate())).remove(building);
                        }
                        buildingList.clear();
                    }
                } // Fin While
            }
        }
        System.out.println("Number of buildings used for microgrids : " + compteur);

        // Récupération des batiments perdus
        System.out.println(losts.size() + " buildings lost !");
        Coordinate key = null;
        List<Microgrid> neighborhoodList = new ArrayList<>();
        compteur = 0;
        for (Building buildingLost : losts) {
            final Coordinate currentKey = hashForGrid(buildingLost.getGeometry().getCoordinate());
            if (currentKey != key) {
                neighborhoodList = loadNeighborhood(currentKey, microgridMap);
                key = currentKey;
            }

            double dist = Double.MAX_VALUE;
            Microgrid tmp_grid = null;
            for (Microgrid microgrid : neighborhoodList) {
                double tmp_dist = buildingLost.getGeometry().getCoordinate().distance(microgrid.getCentroid());
                if (tmp_dist < dist) {
                    tmp_grid = microgrid;
                    dist = tmp_dist;
                }
            }
            if (tmp_grid != null && dist < DISTANCE_MAX * 1.1) {
                ++compteur;
                tmp_grid.addBuilding(buildingLost);
            } else {
                context.add(buildingLost);
                geography.move(buildingLost, buildingLost.getGeometry());
            }
        }
        System.out.println(compteur + " buildings recovered");

        System.out.println("****** End of microgrids' creation ******");
    }


    /**
     * Chargement des cases voisines a la case donc les coordonnees sont "coord"
     *
     * @param point Coordonnee d'une case de la grille
     * @param map   map dans laquelle on va chercher les voisins
     *
     * @return La liste de voisins
     */
    public <T> List<T> loadNeighborhood(Coordinate point, Map<Coordinate, List<T>> map) {
        List<T> neighborhoodList = new ArrayList<>();

        Coordinate coord = new Coordinate(point.x + GRID_DIMENSION, point.y);       // Est
        if (buildingMap.containsKey(coord)) neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x + GRID_DIMENSION, point.y + GRID_DIMENSION); // Sud-Est
        if (buildingMap.containsKey(coord)) neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x, point.y + GRID_DIMENSION);                  // Sud
        if (buildingMap.containsKey(coord)) neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x - GRID_DIMENSION, point.y + GRID_DIMENSION); // Sud-Ouest
        if (buildingMap.containsKey(coord)) neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x - GRID_DIMENSION, point.y);                  // Ouest
        if (buildingMap.containsKey(coord)) neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x - GRID_DIMENSION, point.y - GRID_DIMENSION); // Nord-Ouest
        if (buildingMap.containsKey(coord)) neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x, point.y - GRID_DIMENSION);                  // Nord
        if (buildingMap.containsKey(coord)) neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x + GRID_DIMENSION, point.y - GRID_DIMENSION); // Nord-Est
        if (buildingMap.containsKey(coord)) neighborhoodList.addAll(map.get(coord));

        return neighborhoodList;
    }


    /**
     * Retourne les coordonnees du coin superieur gauche de la case dans laquelle se trouve 'coord'
     *
     * @param coord Coordonnees d'un batiment/microgrid/...
     *
     * @return les coordonnees du coin superieur gauche de la case dans laquelle se trouve 'coord'
     */
    public static Coordinate hashForGrid(Coordinate coord) {
        double x = LONG_MIN;
        double y = LAT_MIN;

        while (coord.x * 1000 > x) {
            x += GRID_DIMENSION;
        }
        x -= GRID_DIMENSION;
        while (coord.y * 1000 > y) {
            y += GRID_DIMENSION;
        }
        y -= GRID_DIMENSION;

        return new Coordinate(x, y);
    }
}