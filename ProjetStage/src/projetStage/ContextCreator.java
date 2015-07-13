package projetStage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import projetStage.agents.Building;
import projetStage.agents.IndustrialBuilding;
import projetStage.agents.Microgrid;
import projetStage.agents.NoticeableBuilding;
import projetStage.agents.SportBuilding;
import projetStage.agents.UndifferentiatedBuilding;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;

public class ContextCreator implements ContextBuilder<Object> {

    final int NB_BUILDING_MIN = 2;
    int NB_BUILDING_MAX;
    double DISTANCE_MAX;
    final double GRID_DIMENSION = 0.01;

    // Coordonnées de l'île de la Réunion
    final double LONG_MIN = 55.215;
    final double LONG_MAX = 55.837;
    final double LAT_MIN = -21.389;
    final double LAT_MAX = -20.871;

    /*final float PERCENT_NOTIC = 10;
    final float PERCENT_INDUS = 10;
    final float PERCENT_SPORT = 10;
    final float PERCENT_UNDIF = 70;*/

    final Map<Coordinate, List<Building>> buildingMap = new HashMap<>();
    final Map<Coordinate, List<Microgrid>> microgridMap = new HashMap<>();

    public Context<Object> build(Context<Object> context) {
        System.out.println("****** Initialisation ******");
        final GeographyParameters<Object> geoParams = new GeographyParameters<>();
        final Geography<Object> geography = GeographyFactoryFinder.createGeographyFactory(null).createGeography("Geography", context, geoParams);

        for (double x = (int)(LONG_MIN * 1000); x < (int)(LONG_MAX * 1000); x += (int)(GRID_DIMENSION * 1000)) {
            for (double y = (int)(LAT_MIN * 1000); y < (int)(LAT_MAX * 1000); y += (int)(GRID_DIMENSION * 1000)) {
                buildingMap.put(new Coordinate(x / 1000, y / 1000), new ArrayList<Building>());
                microgridMap.put(new Coordinate(x / 1000, y / 1000), new ArrayList<Microgrid>());
            }
        }

        //System.out.println(RequestsHttp.getData());

        long time = System.currentTimeMillis();

        readParameters();
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

        for (String type : types) {
            for (String ville : cities) {
                loadFeatures("../data/buildings/" + ville + "/" + type + "/building-" + type + "-" + ville + ".shp");
            }
        }

        NB_BUILDING_MAX = params.getInteger("NB_BUILDING_MAX");
        DISTANCE_MAX = params.getDouble("DISTANCE_MAX");
    }

    /**
     * Chargement des fichier shapefile
     *
     * @param filename Nom du fichier a charger
     */
    private void loadFeatures(final String filename) {
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
     * @param context Context
     * @param geography Geography
     */
    public void buildMicrogrid(final Context<Object> context, final Geography<Object> geography) {
        System.out.println("****** Microgrids' creation ******");
        final List<Building> buildingList = new ArrayList<>(); //
        final List<Building> losts = new ArrayList<>();
        int compteur = 0;

        for (Coordinate key : buildingMap.keySet()) {
            List<Building> actualGridBuildingList = buildingMap.get(key);

            if (!actualGridBuildingList.isEmpty()) {
                // Recuperation de la liste des cases voisines sur la grille
                List<Building> neighborhoodList = loadNeighborhood(key, buildingMap);

                // Debut
                while (!actualGridBuildingList.isEmpty()) {
                    // On cherche les elements les plus proches entre eux
                    Coordinate barycentre = null;
                    for (Building building : neighborhoodList) {
                        if (barycentre == null) {
                            buildingList.add(building);
                            barycentre = getCentroid(buildingList);
                        } else {
                            double dist = building.getGeometry().getCoordinate().distance(barycentre);
                            if (buildingList.size() < NB_BUILDING_MAX) { // S'il reste de la place dans la liste
                                if (dist < DISTANCE_MAX) { // S'il n'est pas trop loin
                                    buildingList.add(building);
                                    barycentre = getCentroid(buildingList);
                                }
                            } else if (dist < DISTANCE_MAX * 1.5) {
                                buildingList.add(building);
                                removeFarthest(buildingList);
                                barycentre = getCentroid(buildingList);
                            }
                        }
                    }

                    if (buildingList.size() < NB_BUILDING_MIN) { // On stocke les batiments les plus isolés dans une liste
                        losts.addAll(buildingList);
                    } else { // On cree la microgrid avec les batiments selectionnes
                        compteur += buildingList.size();
                        Microgrid microgrid = new Microgrid(context, geography, buildingList, barycentre);
                        microgridMap.get(hashForGrid(microgrid.getCentroid())).add(microgrid);
                        context.add(microgrid);
                    }
                    System.out.println("size : " + buildingList.size());
                    System.out.println("compteur : " + compteur);

                    // On supprime les batiments selectionnes de la liste des features
                    actualGridBuildingList.removeAll(buildingList);
                    neighborhoodList.removeAll(buildingList);
                    removeFromGrid(buildingMap, buildingList);
                    buildingList.clear();
                }
            }
        }

        // Récupération des batiments perdus
        System.out.println("Récupération de " + losts.size() + " batiments");
        Coordinate key = null;
        List<Microgrid> neighborhoodList = new ArrayList<>();
        for (Building building : losts) {
            final Coordinate currentKey = hashForGrid(building.getGeometry().getCoordinate());
            if(currentKey != key) {
                neighborhoodList = loadNeighborhood(currentKey, microgridMap);
                key = currentKey;
            }

            double dist = Double.MAX_VALUE;
            Microgrid tmp_grid = null;
            for(Microgrid microgrid : neighborhoodList) {
                double tmp_dist = building.getGeometry().getCoordinate().distance(microgrid.getCentroid());
                if(tmp_dist < dist) {
                    tmp_grid = microgrid;
                    dist = tmp_dist;
                }
            }
            if(tmp_grid != null && dist < DISTANCE_MAX) {
                tmp_grid.addBuilding(building);
            } else {
                context.add(building);
                geography.move(building, building.getGeometry());
            }
        }

        System.out.println("****** End of microgrids' creation ******");
    }

    /**
     * Chargement des cases voisines a la case donc les coordonnees sont "coord"
     *
     * @param coord Coordonnee d'une case de la grille
     * @param map   map dans laquelle on va chercher les voisins
     *
     * @return La liste de voisins
     */
    public <T> List<T> loadNeighborhood(Coordinate coord, Map<Coordinate, List<T>> map) {
        List<T> neighborhoodList = new ArrayList<>();

        neighborhoodList.addAll(map.get(coord));
        if (buildingMap.containsKey(new Coordinate(coord.x + GRID_DIMENSION, coord.y))) // Est
            neighborhoodList.addAll(map.get(new Coordinate(coord.x + GRID_DIMENSION, coord.y)));
        if (buildingMap.containsKey(new Coordinate(coord.x + GRID_DIMENSION, coord.y + GRID_DIMENSION))) // Sud-Est
            neighborhoodList.addAll(map.get(new Coordinate(coord.x + GRID_DIMENSION, coord.y + GRID_DIMENSION)));
        if (buildingMap.containsKey(new Coordinate(coord.x, coord.y + GRID_DIMENSION))) // Sud
            neighborhoodList.addAll(map.get(new Coordinate(coord.x, coord.y + GRID_DIMENSION)));
        if (buildingMap.containsKey(new Coordinate(coord.x - GRID_DIMENSION, coord.y + GRID_DIMENSION))) // Sud-Ouest
            neighborhoodList.addAll(map.get(new Coordinate(coord.x - GRID_DIMENSION, coord.y + GRID_DIMENSION)));
        if (buildingMap.containsKey(new Coordinate(coord.x - GRID_DIMENSION, coord.y))) // Ouest
            neighborhoodList.addAll(map.get(new Coordinate(coord.x - GRID_DIMENSION, coord.y)));
        if (buildingMap.containsKey(new Coordinate(coord.x - GRID_DIMENSION, coord.y - GRID_DIMENSION))) // Nord-Ouest
            neighborhoodList.addAll(map.get(new Coordinate(coord.x - GRID_DIMENSION, coord.y - GRID_DIMENSION)));
        if (buildingMap.containsKey(new Coordinate(coord.x, coord.y - GRID_DIMENSION))) // Nord
            neighborhoodList.addAll(map.get(new Coordinate(coord.x, coord.y - GRID_DIMENSION)));
        if (buildingMap.containsKey(new Coordinate(coord.x + GRID_DIMENSION, coord.y - GRID_DIMENSION))) // Nord-Est
            neighborhoodList.addAll(map.get(new Coordinate(coord.x + GRID_DIMENSION, coord.y - GRID_DIMENSION)));

        return neighborhoodList;
    }

    /**
     * Calcule le barycentre des batiments contenus dans buildingList et supprime le batiment le plus eloigne du barycentre
     *
     * @param buildingList Une liste de batiments
     */
    public void removeFarthest(final List<Building> buildingList) {
        final Coordinate center = getCentroid(buildingList);

        Building farthest = null;
        double dist = 0;
        for (Building building : buildingList) {
            final double tmp = building.getGeometry().getCentroid().getCoordinate().distance(center);
            if (tmp > dist) {
                dist = tmp;
                farthest = building;
            }
        }
        buildingList.remove(farthest);
    }

    /**
     * Retourne les coordonnees du coin superieur gauche de la case dans laquelle se trouve 'coord'
     *
     * @param coord Coordonnees d'un batiment
     *
     * @return les coordonnees du coin superieur gauche de la case dans laquelle se trouve 'coord'
     */
    public Coordinate hashForGrid(Coordinate coord) {
        double x = LONG_MIN * 1000;
        double y = LAT_MIN * 1000;

        while (coord.x > x / 1000) {
            x += GRID_DIMENSION * 1000;
        }
        x -= GRID_DIMENSION * 1000;
        while (coord.y > y / 1000) {
            y += GRID_DIMENSION * 1000;
        }
        y -= GRID_DIMENSION * 1000;

        return new Coordinate(x / 1000, y / 1000);
    }

    /**
     * Supprime la liste de batiments 'buildingList' de 'buildingsMaps'
     *
     * @param buildingsMap Map contenant tous les batiments a charger
     * @param buildingList Liste des batiments a supprimer
     */
    public void removeFromGrid(Map<Coordinate, List<Building>> buildingsMap, List<Building> buildingList) {
        for (Building building : buildingList) {
            buildingsMap.get(hashForGrid(building.getGeometry().getCentroid().getCoordinate())).remove(building);
        }
    }

    /**
     * Calcule le barycentre des batiments présents dans buildingList
     *
     * @param buildingList La list de batiments
     *
     * @return Le barycentre des batiments
     */
    public Coordinate getCentroid(List<Building> buildingList) {
        Geometry buildingCenters = new Polygon(null, null, new GeometryFactory());
        for (Building building : buildingList) {
            buildingCenters = buildingCenters.union(building.getGeometry().getCentroid());
        }
        return buildingCenters.getCentroid().getCoordinate();
    }
}

	/*int i = 0;
    List<Polygon> polyList = new ArrayList<>();
	// For each feature in the file
	for (SimpleFeature feature : features){
		if(i < 20) {
			Geometry geom = (Geometry)feature.getDefaultGeometry();
			Object agent = null;
			
			
			// For Polygons, create ZoneAgents
			if (geom instanceof MultiPolygon){
				MultiPolygon mp = (MultiPolygon)feature.getDefaultGeometry();
				geom = (Polygon)mp.getGeometryN(0);

				polyList.add((Polygon)geom);
				
				String id = (String)feature.getAttribute("ID");
				
				agent = new Building(id);
			}
			++i;

			if (agent != null){
				context.add(agent);
				geography.move(agent, geom);
				//System.out.println("Creating agent for  " + geom);
			}
			else{
				System.out.println("Error creating agent for  " + geom);
			}
		}
	}
	Microgrid microgrid = new Microgrid(context, geography, polyList);
	context.add(microgrid);*/
