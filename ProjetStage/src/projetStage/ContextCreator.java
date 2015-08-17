package projetStage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import com.vividsolutions.jts.geom.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import projetStage.agents.Building;
import projetStage.agents.IndustrialBuilding;
import projetStage.agents.Microgrid;
import projetStage.agents.NoticeableBuilding;
import projetStage.agents.SportBuilding;
import projetStage.agents.UndifferentiatedBuilding;

import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;

public class ContextCreator implements ContextBuilder<Object> {
    private final int NB_BUILDING_MIN = 2;
    private int NB_BUILDING_MAX;
    private double DISTANCE_MAX;
    private double GRID_DIMENSION;

    // Coordonnées de l'île de la Réunion *1000
    private final double LONG_MIN = 55215;
    private final double LONG_MAX = 55837;
    private final double LAT_MIN = -21389;
    private final double LAT_MAX = -20871;

    private final Map<Coordinate, List<Building>> buildingMap = new HashMap<>();
    private final Map<Coordinate, List<Microgrid>> microgridMap = new HashMap<>();
    private final TreeMap<Double, List<Coordinate>> keyMap = new TreeMap<>();

    public Context<Object> build(Context<Object> context) {
        System.out.println("****** Initialisation ******");
        final GeographyParameters<Object> geoParams = new GeographyParameters<>();
        final Geography<Object> geography = GeographyFactoryFinder.createGeographyFactory(null).createGeography("Geography", context, geoParams);

        //System.out.println(RequestsHttp.getData());

        long time = System.currentTimeMillis();

        // Lecture des paramètres
        readParameters();

        // Création des microgrids
        buildMicrogrid(context, geography);

        System.out.println("Time : " + (System.currentTimeMillis() - time));

        return context;
    }

    /**
     * Lecture des parametres et chargement des batiments
     */
    public void readParameters() {
        Parameters params = RunEnvironment.getInstance().getParameters();
        List<String> types = new ArrayList<>();
        List<String> cities = new ArrayList<>();

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

        NB_BUILDING_MAX = params.getInteger("NB_BUILDING_MAX");
        DISTANCE_MAX = params.getDouble("DISTANCE_MAX");
        GRID_DIMENSION = DISTANCE_MAX * 2 * 1000;

        System.out.println("GRID_DIMENSION : " + GRID_DIMENSION);

        // Création de la grille
        for (double x = LONG_MIN; x < LONG_MAX; x += GRID_DIMENSION) {
            for (double y = LAT_MIN; y < LAT_MAX; y += GRID_DIMENSION) {
                Coordinate coord = new Coordinate(x, y);
                buildingMap.put(coord, new ArrayList<Building>());
                microgridMap.put(coord, new ArrayList<Microgrid>());
            }
        }

        // Chargement des batiments
        for (String type : types) {
            for (String ville : cities) {
                loadFeatures("./data/buildings/" + ville + "/" + type + "/building-" + type + "-" + ville + ".shp");
            }
        }

        // Récupération des cases de la grille non vide et triage en fonction de leur distance au centre de la carte
        Coordinate center = new Coordinate((LONG_MAX + LONG_MIN) / 2, (LAT_MAX + LAT_MIN) / 2);
        Iterator<Map.Entry<Coordinate, List<Building>>> ite = buildingMap.entrySet().iterator();
        while (ite.hasNext()) {
            Map.Entry<Coordinate, List<Building>> entry = ite.next();
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
     * Chargement des fichier shapefile
     *
     * @param filename Nom du fichier a charger
     */
    private void loadFeatures(final String filename) {
        System.out.println("-> Chargement " + filename);
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
                buildingMap.get(hashForGrid(geom.getCentroid().getCoordinate())).add(building);
            }

            fiter.close();
            store.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Construction des microgrids
     *
     * @param context   Context
     * @param geography Geography
     */
    public void buildMicrogrid(final Context<Object> context, final Geography<Object> geography) {
        System.out.println("****** Microgrids' creation ******");
        final List<Building> buildingList = new ArrayList<>(); //
        final List<Building> losts = new ArrayList<>();
        int compteur = 0;
        int compteurGrid = 0;

        Iterator<Map.Entry<Double, List<Coordinate>>> it = keyMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Double, List<Coordinate>> entry = it.next();
            for (Coordinate coord : entry.getValue()) {
                List<Building> actualGridBuildingList = buildingMap.get(coord);

                if (!actualGridBuildingList.isEmpty()) {
                    // Recuperation de la liste des cases voisines sur la grille
                    List<Building> neighborhoodList = loadNeighborhood(coord, buildingMap);
                    neighborhoodList.addAll(actualGridBuildingList);

                    // Debut While
                    while (!actualGridBuildingList.isEmpty()) {
                        // On prend un batiment au hasard
                        Coordinate center = actualGridBuildingList.get(0).getGeometry().getCentroid().getCoordinate();
                        buildingList.add(actualGridBuildingList.get(0));
                        buildingList.get(0).setDistance(0);
                        neighborhoodList.remove(actualGridBuildingList.get(0));

                        // On prend tous les batiments qui sont à une distance inférieure à DISTANCE_MAX
                        for (Building building : neighborhoodList) {
                            building.setDistance(building.getGeometry().getCoordinate().distance(center));
                            if (building.getDistance() < DISTANCE_MAX)
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
                        if (buildingList.size() < NB_BUILDING_MIN) {
                            // On stocke les batiments les plus isolés dans une liste pour les retraiter ensuite
                            losts.addAll(buildingList);
                        } else {
                            // On crée la microgrid avec les batiments selectionnés
                            compteur += buildingList.size();
                            compteurGrid += 1;
                            Microgrid microgrid = new Microgrid(context, geography, compteurGrid, buildingList, center);
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
        System.out.println("compteur fin : " + compteur);

        // Récupération des batiments perdus
        System.out.println("Tentative de récupération de " + losts.size() + " batiments");
        Coordinate key = null;
        List<Microgrid> neighborhoodList = new ArrayList<>();
        compteur = 0;
        for (Building building : losts) {
            final Coordinate currentKey = hashForGrid(building.getGeometry().getCoordinate());
            if (currentKey != key) {
                neighborhoodList = loadNeighborhood(currentKey, microgridMap);
                key = currentKey;
            }

            double dist = Double.MAX_VALUE;
            Microgrid tmp_grid = null;
            for (Microgrid microgrid : neighborhoodList) {
                double tmp_dist = building.getGeometry().getCoordinate().distance(microgrid.getCentroid());
                if (tmp_dist < dist) {
                    tmp_grid = microgrid;
                    dist = tmp_dist;
                }
            }
            if (tmp_grid != null && dist < DISTANCE_MAX) {
                ++compteur;
                tmp_grid.addBuilding(building);
            } else {
                context.add(building);
                geography.move(building, building.getGeometry());
            }
        }
        System.out.println(compteur + " batiments récupérés");

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

        Coordinate coord = new Coordinate(point.x + GRID_DIMENSION, point.y); // Est
        if (buildingMap.containsKey(coord))
            neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x + GRID_DIMENSION, point.y + GRID_DIMENSION); // Sud-Est
        if (buildingMap.containsKey(coord))
            neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x, point.y + GRID_DIMENSION); // Sud
        if (buildingMap.containsKey(coord))
            neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x - GRID_DIMENSION, point.y + GRID_DIMENSION); // Sud-Ouest
        if (buildingMap.containsKey(coord))
            neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x - GRID_DIMENSION, point.y); // Ouest
        if (buildingMap.containsKey(coord))
            neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x - GRID_DIMENSION, point.y - GRID_DIMENSION); // Nord-Ouest
        if (buildingMap.containsKey(coord))
            neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x, point.y - GRID_DIMENSION); // Nord
        if (buildingMap.containsKey(coord))
            neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x + GRID_DIMENSION, point.y - GRID_DIMENSION);// Nord-Est
        if (buildingMap.containsKey(coord))
            neighborhoodList.addAll(map.get(coord));

        return neighborhoodList;
    }


    /**
     * Retourne les coordonnees du coin superieur gauche de la case dans laquelle se trouve 'coord'
     *
     * @param coord Coordonnees d'un batiment
     *
     * @return les coordonnees du coin superieur gauche de la case dans laquelle se trouve 'coord'
     */
    public Coordinate hashForGrid(Coordinate coord) {
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