package projetStage.agents;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;

public class Microgrid {
    private Context<Object> context;
    private Geography<Object> geography;
    private List<Building> buildingList;    // Liste des batiments contenus dans cette microgrid
    private Geometry convexHull;    // Plus petit polygone convexe contenant tous les batiments de la microgrid
    private LineString line;    // Ligne représentant convexHull
    private Coordinate centroid;    // Barycentre de la microgrid
    private int nbBatiments = 0;

    /**
     * Constructeur par défaut
     *
     * @param context   Context
     * @param geography Geography
     */
    public Microgrid(Context<Object> context, Geography<Object> geography) {
        this.context = context;
        this.geography = geography;
        buildingList = new ArrayList<>();
        convexHull = new Polygon(null, null, new GeometryFactory());
        line = null;
    }

    /**
     * Constructeur paramétré, création du plus petit polygone convexe contenant tous les batiments
     * contenus dans featureList
     *
     * @param context     Context
     * @param geography   Geography
     * @param featureList Liste des batiment à ajouter à la microgrid
     */
    public Microgrid(Context<Object> context, Geography<Object> geography, List<Building> featureList, Coordinate centroid) {
        this(context, geography);
        setCentroid(centroid);

        Geometry centerList = new Polygon(null, null, new GeometryFactory());
        for (Building building : featureList) {
            Geometry geom = building.getGeometry();
            buildingList.add(building);
            //context.add(building);
            //geo.move(building, geom);
            centerList = centerList.union(geom.getCentroid());
        }
        nbBatiments = buildingList.size();
        convexHull = centerList.convexHull();
        buildMicrogrid();
    }

    /**
     * Construction visuelle de la microgrid
     */
    public void buildMicrogrid() {
        final Coordinate[] coordinateArray = convexHull.getCoordinates();
        if (coordinateArray.length >= 2) {
            if (line != null) {
                context.remove(line);
            }
            line = new LineString(new CoordinateArraySequence(coordinateArray), new GeometryFactory());
            context.add(line);
            geography.move(this, line);
        } else {
            final Geography<Object> geo = (Geography<Object>) context.getProjection("Geography");
            context.add(buildingList.get(0));
            geo.move(buildingList.get(0), buildingList.get(0).getGeometry());
        }
    }

    /**
     * AJout d'un batiment dans la microgrid
     *
     * @param building Batiment à ajouter
     */
    public void addBuilding(Building building) {
        buildingList.add(building);
        nbBatiments = buildingList.size();
        if (!convexHull.contains(building.getGeometry()))
            convexHull = convexHull.union(building.getGeometry()).convexHull();

        buildMicrogrid();
    }

    public Coordinate getCentroid() {
        return centroid;
    }

    public void setCentroid(Coordinate centroid) {
        this.centroid = centroid;
    }

    public int getNbBatiments() {
        return nbBatiments;
    }

    public void setNbBatiments(int nbBatiments) {
        this.nbBatiments = nbBatiments;
    }
}
