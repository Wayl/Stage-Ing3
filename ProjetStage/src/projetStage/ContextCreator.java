package projetStage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;
import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.CircularRing;
import org.geotools.geometry.jts.JTSFactoryFinder;
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

    // Coordonnées de l'île de la Réunion
    private final double LONG_MIN = 55215;
    private final double LONG_MAX = 55837;
    private final double LAT_MIN = -21389;
    private final double LAT_MAX = -20871;

    /*final float PERCENT_NOTIC = 10;
    final float PERCENT_INDUS = 10;
    final float PERCENT_SPORT = 10;
    final float PERCENT_UNDIF = 70;*/

    private final Map<Coordinate, List<Building>> buildingMap = new HashMap<>();
    private final Map<Coordinate, List<Microgrid>> microgridMap = new HashMap<>();

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
                buildingMap.put(new Coordinate(x, y), new ArrayList<Building>());
                microgridMap.put(new Coordinate(x, y), new ArrayList<Microgrid>());
            }
        }

        for (String type : types) {
            for (String ville : cities) {
                loadFeatures("./data/buildings/" + ville + "/" + type + "/building-" + type + "-" + ville + ".shp");
            }
        }

    }

    /**
     * Chargement des fichier shapefile
     * a
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
     * @param context   Context
     * @param geography Geography
     */
    public void buildMicrogrid(final Context<Object> context, final Geography<Object> geography) {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

        System.out.println("****** Microgrids' creation ******");
        final List<Building> buildingList = new ArrayList<>(); //
        final List<Building> losts = new ArrayList<>();
        int compteur = 0;
        int compteurGrid = 0;

        for (Coordinate key : buildingMap.keySet()) {
            List<Building> actualGridBuildingList = buildingMap.get(key);

            if (!actualGridBuildingList.isEmpty()) {
                // Recuperation de la liste des cases voisines sur la grille
                List<Building> neighborhoodList = loadNeighborhood(key, buildingMap);
                neighborhoodList.addAll(actualGridBuildingList);

                // Debut
                while (!actualGridBuildingList.isEmpty()) {
                    // On cherche les elements les plus proches entre eux
                    Coordinate center = null;
                    for (Building building : neighborhoodList) {
                        if (center == null) {
                            buildingList.add(building);
                            center = building.getGeometry().getCentroid().getCoordinate();
                        } else {
                            double dist = building.getGeometry().getCoordinate().distance(center);
                            if (dist < DISTANCE_MAX) {
                                buildingList.add(building);
                                if (buildingList.size() > NB_BUILDING_MAX) {
                                    removeFarthest(buildingList, center);
                                }
                                center = getCenter(buildingList);
//                                Circle circle = WelzlSphere(buildingList, buildingList.size(), new ArrayList<Building>(), 0);
//                                Circle circle = getCenter(buildingList, new ArrayList<Building>());
//                                center = new Coordinate(circle.getCenterX(), circle.getCenterY());
//                                System.out.println("DONE !!! " + center);
                            }
                        }
                    }

                    if (buildingList.size() < NB_BUILDING_MIN) { // On stocke les batiments les plus isolés dans une liste pour les retraiter ensuite
                        //System.out.println("trop petit ! (" + buildingList.size() + ")");
                        losts.addAll(buildingList);
                    } else { // On cree la microgrid avec les batiments selectionnes
                        compteur += buildingList.size();
                        compteurGrid += 1;
                        Microgrid microgrid = new Microgrid(context, geography, compteurGrid, buildingList, center);
                        //System.out.println(hashForGrid(microgrid.getCentroid()));
                        //System.out.println(microgridMap.get(hashForGrid(microgrid.getCentroid())));
                        microgridMap.get(hashForGrid(microgrid.getCentroid())).add(microgrid);
                        context.add(microgrid);
                    }
                    //System.out.println("compteur : " + compteur);

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
     * @param point Coordonnee d'une case de la grille
     * @param map   map dans laquelle on va chercher les voisins
     *
     * @return La liste de voisins
     */
    public <T> List<T> loadNeighborhood(Coordinate point, Map<Coordinate, List<T>> map) {
        List<T> neighborhoodList = new ArrayList<>();

        Coordinate coord = new Coordinate(point.x + GRID_DIMENSION, point.y);
        if (buildingMap.containsKey(coord)) // Est
            neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x + GRID_DIMENSION, point.y + GRID_DIMENSION); // Sud-Est
        if (buildingMap.containsKey(coord))
            neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x, point.y + GRID_DIMENSION);
        if (buildingMap.containsKey(coord)) // Sud
            neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x - GRID_DIMENSION, point.y + GRID_DIMENSION);
        if (buildingMap.containsKey(coord)) // Sud-Ouest
            neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x - GRID_DIMENSION, point.y);
        if (buildingMap.containsKey(coord)) // Ouest
            neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x - GRID_DIMENSION, point.y - GRID_DIMENSION);
        if (buildingMap.containsKey(coord)) // Nord-Ouest
            neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x, point.y - GRID_DIMENSION);
        if (buildingMap.containsKey(coord)) // Nord
            neighborhoodList.addAll(map.get(coord));
        coord = new Coordinate(point.x + GRID_DIMENSION, point.y - GRID_DIMENSION);
        if (buildingMap.containsKey(coord)) // Nord-Est
            neighborhoodList.addAll(map.get(coord));

        return neighborhoodList;
    }
    /*public <T> List<T> loadNeighborhood(Coordinate center, Map<Coordinate, List<T>> map) {
        List<T> neighborhoodList = new ArrayList<>();

        HashSet<Coordinate> coordinates = circle(center, DISTANCE_MAX * 1000);
        //System.out.println("nb cases selectionnées : " + coordinates.size());
        for (Coordinate coord : coordinates) {
            if (buildingMap.containsKey(coord)) {
                neighborhoodList.addAll(map.get(coord));
            }
        }

        return neighborhoodList;
    }*/

    public HashSet<Coordinate> circle(Coordinate centre, double rayon) {
        //System.out.println("rayon : " + rayon);
        HashSet<Coordinate> pixels = new HashSet<>();
        double taillePixel = GRID_DIMENSION;
        double x = 0;
        double y = rayon;
        double d = rayon - 1;

        while (y >= x) {
            pixels.add(new Coordinate(centre.x + y * taillePixel, centre.y + x * taillePixel));
            pixels.add(new Coordinate(centre.x + x * taillePixel, centre.y + y * taillePixel));
            pixels.add(new Coordinate(centre.x - x * taillePixel, centre.y + y * taillePixel));
            pixels.add(new Coordinate(centre.x - y * taillePixel, centre.y + x * taillePixel));
            pixels.add(new Coordinate(centre.x + x * taillePixel, centre.y - y * taillePixel));
            pixels.add(new Coordinate(centre.x + y * taillePixel, centre.y - x * taillePixel));
            pixels.add(new Coordinate(centre.x - x * taillePixel, centre.y - y * taillePixel));
            pixels.add(new Coordinate(centre.x - y * taillePixel, centre.y - x * taillePixel));

            if (d >= 2 * x) {
                d -= 2 * x + 1;
                x++;
            } else if (d < 2 * (rayon - y)) {
                d += 2 * y - 1;
                y--;
            } else {
                d += 2 * (y - x - 1);
                y--;
                x++;
            }
        }

        return pixels;
    }

    /**
     * Calcule le barycentre des batiments contenus dans buildingList et supprime le batiment le plus eloigne du barycentre
     *
     * @param buildingList Une liste de batiments
     */
    public void removeFarthest(final List<Building> buildingList, Coordinate center) {
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


    /**
     * Calcule une approximation du centre d'un ensemble de point et cherchant les 2 points les plus éloignés
     *
     * @param buildingList liste des batiments
     *
     * @return
     */
    public Coordinate getCenter(List<Building> buildingList) {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        //CurvedGeometryFactory curvedFactory = new CurvedGeometryFactory(geometryFactory, Double.MAX_VALUE);

        Geometry buildingCenters = new Polygon(null, null, geometryFactory);
        for (Building building : buildingList) {
            buildingCenters = buildingCenters.union(building.getGeometry().getCentroid());
        }

        double distMax = 0;
        Coordinate tmpi = null;
        Coordinate tmpj = null;
        final Coordinate[] coordinates = buildingCenters.getCoordinates();
        for (int i = 0; i < coordinates.length - 1; ++i) {
            for (int j = i + 1; j < coordinates.length; ++j) {
                final double dist = coordinates[i].distance(coordinates[j]);
                if (dist > distMax) {
                    distMax = dist;
                    tmpi = coordinates[i];
                    tmpj = coordinates[j];
                }
            }
        }

        if (distMax > DISTANCE_MAX * 2)
            System.out.println("AAARG " + distMax);
        return new Coordinate((tmpi.x + tmpj.x) / 2, (tmpi.y + tmpj.y) / 2);
    }

    /**
     * Algorithme de Welzl (https://fr.wikipedia.org/wiki/Problème_du_cercle_minimum)
     *
     * @return
     */
    public Circle getCenter(List<Building> P, List<Building> R) {
        Circle circle;
        //System.out.println("P : " + P.size() + ", R : " + R.size());

        if (P.isEmpty() || R.size() == 3) {
            //System.out.println("aze");

            /*CoordinateSequence coordinateSequence;
            if (R.size() <= 1) {
                coordinateSequence = new PackedCoordinateSequence.Double(new double[]{0.5, 0, 0, 0.5, 0.5, 1, 1, 0.5, 0.5, 0}, 2);
            } else {
                coordinateSequence = new CoordinateArraySequence(R.size() + 1, 2);
                for (int i = 0; i < R.size(); ++i) {
                    coordinateSequence.setOrdinate(i, 0, R.get(i).getGeometry().getCoordinate().x);
                    coordinateSequence.setOrdinate(i, 1, R.get(i).getGeometry().getCoordinate().y);
                }
                coordinateSequence.setOrdinate(R.size(), 0, R.get(0).getGeometry().getCoordinate().x);
                coordinateSequence.setOrdinate(R.size(), 1, R.get(0).getGeometry().getCoordinate().y);
            }*/
            circle = createCircle(R);
        } else {
            //System.out.println("qsd");
            Building building = P.get((int)Math.floor(Math.random() * P.size()));

            List<Building> ptemp = new LinkedList<>(P);
            ptemp.remove(building);
            circle = getCenter(ptemp, R);

            //System.out.println(circle.contains(building.getGeometry()) + ", " + circle.getArea());
            Point coord = building.getGeometry().getCentroid();
            if (!circle.contains(new Point2D(coord.getX(), coord.getY()))) {
                //System.out.println("yop");
                List<Building> rtemp = new LinkedList<>(R);
                rtemp.add(building);
                circle = getCenter(P, rtemp);
            }
        }

        return circle;
    }

    public Circle WelzlSphere(List<Building> pt, int numPts, List<Building> sos, int numSos) {
        System.out.println("\nsize : " + pt.size() + ", numpts : " + numPts);
        // if no input points, the recursion has bottomed out. Now compute an exact sphere
        // based on points in set of support (zero through four points)
        if (numPts == 0) {
            return createCircle(sos);
        }

        // Pick a point at "random"
        int index = numPts - 1;
        System.out.println("index : " + index);
        // Recursively compute the smallest bounding sphere of the remaining points
        Circle smallestSphere = WelzlSphere(pt, numPts - 1, sos, numSos);
        System.out.println(smallestSphere.getCenterX() + ", " + smallestSphere.getCenterY() + " => " + smallestSphere.getRadius());
        // If the selected point lies inside this sphere, it is indeed the smallest
        Point coord = pt.get(index).getGeometry().getCentroid();
        System.out.println(coord);
        if (smallestSphere.contains(new Point2D(coord.getX(), coord.getY()))) {
            System.out.println(true);
            return smallestSphere;
        } else {
            System.out.println(false);
        }

        // Otherwise, update set of support to additionally contain the new point
        System.out.println("add index " + index + " : " + pt.get(index));
        if (!sos.contains(pt.get(index)))
            sos.add(pt.get(index));
        // Recursively compute the smallest sphere of remaining points with new s.o.s.
        return WelzlSphere(pt, numPts - 1, sos, numSos + 1);
    }

    /*public CircularRing getCenter(List<Building> S, List<Building> P, List<Building> Q) {
        CircularRing circle;

        // déterminer le cercle minimum de Q
        // transférer les points qui ne sont pas sur le cercle dans Q

        while (!S.isEmpty()) {
            Building building = S.get((int)Math.floor(Math.random() * S.size()));
            S.remove(building);
            if (circle.contains(building.getGeometry())) {
                P.add(building);
            } else {
                List<Building> R = new ArrayList<>();
                R.addAll(Q);
                R.add(building);
                circle = getCenter(circle, S, R, P);
            }
        }

        return circle;
    }*/


    public Circle createCircle(List<Building> buildingList) {
        System.out.println("nb points : " + buildingList.size());
        Circle circle = new Circle();
        switch (buildingList.size()) {
            case 3:
                Coordinate c1 = buildingList.get(0).getGeometry().getCentroid().getCoordinate();
                Coordinate c2 = buildingList.get(1).getGeometry().getCentroid().getCoordinate();
                Coordinate c3 = buildingList.get(2).getGeometry().getCentroid().getCoordinate();
                System.out.println(c1);
                System.out.println(c2);
                System.out.println(c3);
                BigDecimal x1 = new BigDecimal(c1.x);
                BigDecimal y1 = new BigDecimal(c1.y);
                BigDecimal x2 = new BigDecimal(c2.x);
                BigDecimal y2 = new BigDecimal(c2.y);
                BigDecimal x3 = new BigDecimal(c3.x);
                BigDecimal y3 = new BigDecimal(c3.y);
                BigDecimal de = new BigDecimal("2");

                System.out.println("qmldskfj " + y3 + ", " + y2);
                BigDecimal b = ((x3.pow(2)).subtract(x2.pow(2)).add(y3.pow(2)).subtract(y2.pow(2))).divide((y3.subtract(y2)).multiply(de), BigDecimal.ROUND_UP);
                BigDecimal bp = ((x2.pow(2)).subtract(x1.pow(2)).add(y2.pow(2)).subtract(y1.pow(2))).divide((y2.subtract(y1)).multiply(de), BigDecimal.ROUND_UP);
                BigDecimal a = (x3.subtract(x2)).divide(y3.subtract(y2), BigDecimal.ROUND_UP);
                BigDecimal ap = (x2.subtract(x1)).divide(y2.subtract(y1), BigDecimal.ROUND_UP);
                BigDecimal xc = (b.subtract(bp)).divide(ap.subtract(a), BigDecimal.ROUND_UP);
                BigDecimal yc = xc.multiply(a).negate().add(bp);

                circle.setCenterX(xc.doubleValue());
                circle.setCenterY(yc.doubleValue());
                System.out.println(circle.getCenterX() + ", " + circle.getCenterY());

                //circle.setCenterX((((c3.x*c3.x - c2.x*c2.x + c3.y*c3.y - c2.y*c2.y)/(2*(c3.y-c2.y)))-((c2.x*c2.x - c1.x*c1.x + c2.y*c2.y - c1.y*c1.y)/(2*(c2.y-c1.y)))) / (((c2.x-c1.x)/(c2.y-c1.y))-((c3.x-c2.x)/(c3.y-c2.y))));
                //circle.setCenterY(-circle.getCenterX() * ((c2.x - c1.x) / (c2.y - c1.y)) + (c2.x * c2.x - c1.x * c1.x + c2.y * c2.y - c1.y * c1.y) / (2 * (c2.y - c1.y)));
                circle.setRadius(Math.sqrt((x1.subtract(xc).pow(2).add(y1.subtract(yc).pow(2))).doubleValue()));
                break;
            case 2:
                Coordinate center = new Coordinate((buildingList.get(0).getGeometry().getCentroid().getCoordinate().x + buildingList.get(1).getGeometry().getCentroid().getCoordinate().x) / 2,
                        (buildingList.get(0).getGeometry().getCentroid().getCoordinate().y + buildingList.get(1).getGeometry().getCentroid().getCoordinate().y) / 2);
                circle.setCenterX(center.x);
                circle.setCenterY(center.y);
                circle.setRadius(center.distance(buildingList.get(0).getGeometry().getCentroid().getCoordinate()));
                break;
            case 1:
                circle.setCenterX(buildingList.get(0).getGeometry().getCentroid().getCoordinate().x);
                circle.setCenterY(buildingList.get(0).getGeometry().getCentroid().getCoordinate().y);
                circle.setRadius(0);
                break;
            case 0:
                circle.setCenterX(0);
                circle.setCenterY(0);
                circle.setRadius(0);
                break;
            /*default:
                circle.setCenterX(0);
                circle.setCenterY(0);
                circle.setRadius(0);
                break;*/
        }
        System.out.println(buildingList.size() + " circle : " + circle.getCenterX() + ", " + circle.getCenterY() + " => " + circle.getRadius());
        return circle;
    }
}