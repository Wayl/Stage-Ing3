package projetStage.agents;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;

import java.io.*;

/**
 * Created by wayl on 19/08/15 !
 */
public class Producer {
    private String name;
    private String type;
    private double powerMax;
    private double powerUsed;
    private boolean active;


    /**
     * Constructeur par défaut
     *
     * @param name Nom du producteur
     * @param type Type d'énergie utilisée
     * @param powerMax Puissance maximale du producteur en MW
     */
    public Producer(String name, String type, double powerMax) {
        this.name = name;
        this.type = type;
        this.powerMax = powerMax;
        this.powerUsed = 0;
        this.active = true;
    }

    /**
     * Création des producteurs d'électricité
     *
     * @param context   context
     * @param geography geography
     */
    public static void createProducers(final Context<Object> context, final Geography<Object> geography) {
        String filename = "./data/producers/producers.csv";
        System.out.println("-> Chargement Producteurs : " + filename);
        try {
            File file = new File(filename);
            InputStream ips = new FileInputStream(file);
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader br = new BufferedReader(ipsr);
            String ligne;
            while ((ligne = br.readLine()) != null) {
                String[] params = ligne.split(",");
                String name = params[0];
                String type = params[1];
                String x = params[2];
                String y = params[3];
                String power = params[4];
                Producer prod = new Producer(name, type, Double.parseDouble(power));

                context.add(prod);
                Coordinate[] coordinates = new Coordinate[1];
                coordinates[0] = new Coordinate(Double.parseDouble(x + 2), Double.parseDouble(y + 2));
                geography.move(prod, new Point(new CoordinateArraySequence(coordinates), new GeometryFactory()));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getPowerMax() {
        return powerMax;
    }

    public double getPowerUsed() {
        return powerUsed;
    }

    public void setPowerUsed(double powerUsed) {
        if(powerUsed > powerMax)
            this.powerUsed = powerMax;
        else if (powerUsed < 0)
            this.powerUsed = 0;
        else
            this.powerUsed = powerUsed;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
