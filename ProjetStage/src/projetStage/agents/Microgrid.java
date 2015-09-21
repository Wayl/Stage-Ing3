package projetStage.agents;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;

public class Microgrid {
    EnergyManager energyManager;
    private int id;
    private Context<Object> context;
    private Geography<Object> geography;
    private Mark mark;
    private List<Building> buildingList;    // Liste des batiments contenus dans cette microgrid
    private Geometry convexHull;            // Plus petit polygone convexe contenant tous les batiments de la microgrid
    private LineString line;                // Ligne représentant la convexHull
    private Coordinate centroid;            // Barycentre de la microgrid

    private double conso = 0;
    private double prod = 0;

    private int hour;
    private int minute;

    /**
     * Constructeur par défaut
     *
     * @param context   Context
     * @param geography Geography
     */
    public Microgrid(Context<Object> context, Geography<Object> geography, EnergyManager energyManager, int id, Calendar calendar) {
        this.context = context;
        this.geography = geography;
        this.energyManager = energyManager;
        this.id = id;
        hour = calendar.get(Calendar.HOUR);
        minute = calendar.get(Calendar.MINUTE);
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
    public Microgrid(Context<Object> context, Geography<Object> geography, EnergyManager energyManager, int id, Calendar calendar, List<Building> featureList) {
        this(context, geography, energyManager, id, calendar);

        // Initialisation buildingList, centerList, convexHull
        Geometry centerList = new Polygon(null, null, new GeometryFactory());
        for (Building building : featureList) {
            Geometry geom = building.getGeometry();
            buildingList.add(building);
            centerList = centerList.union(geom.getCentroid());
            // Affichage de tous les batiments :
            /*context.add(building);
            geography.move(building, geom);*/
        }
        setCentroid(centerList.getCentroid().getCoordinate());
        convexHull = centerList.convexHull();

        // Création de la marque
        mark = new Mark(featureList.size());
        context.add(mark);
        Coordinate[] coordinates = new Coordinate[1];
        coordinates[0] = getCentroid();
        geography.move(mark, new Point(new CoordinateArraySequence(coordinates), new GeometryFactory()));

        // Construction du polygon représentant la microgrid
        buildMicrogrid();
    }


    /**
     * STEP
     *
     * Méthode effectuée à chaque step
     */
    @ScheduledMethod(start = 0, interval = 1, priority = 1)
    public void step() {
        minute += 1;
        if(minute >= 60) {
            minute = 0;
            ++hour;
            if(hour >= 24)
                hour = 0;
        }

        calcConso();
        calcProd();
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
            final Geography<Object> geo = (Geography<Object>)context.getProjection("Geography");
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
        if (!convexHull.contains(building.getGeometry()))
            convexHull = convexHull.union(building.getGeometry()).convexHull();

        buildMicrogrid();
        updateNbBuilding();
    }

    /**
     * Mise à jour de la marque affichant le nombre de batiments
     */
    public void updateNbBuilding() {
        mark.setNbBuilding(buildingList.size());
    }


    public Coordinate getCentroid() {
        return centroid;
    }

    public void setCentroid(Coordinate centroid) {
        this.centroid = centroid;
    }

    public double getConso() {
        return conso;
    }

    public double getProd() {
        return prod;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public void calcConso() {
        String temps = "";
        if(hour < 10)
            temps += "0";
        temps += hour + ":";
        if(minute < 10)
            temps += "0";
        temps += (minute - minute%5);

        double tmp = 0;
        for(Building building : buildingList) {
            tmp += building.getConso(temps);
        }
        conso = tmp - energyManager.allocateEnergy(tmp, centroid);

        if(conso < tmp)
            mark.setPowerOn(false);
        else
            mark.setPowerOn(true);

    }

    public void calcProd() {
        prod = 0;
    }
}
