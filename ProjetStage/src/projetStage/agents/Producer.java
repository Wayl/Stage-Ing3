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
    private double power;
    private boolean isActive;


    /**
     * Constructeur par défaut
     *
     * @param name Nom du producteur
     * @param type Type d'énergie utilisée
     * @param power Puissance maximale du producteur en MW
     */
    public Producer(String name, String type, double power) {
        this.name = name;
        this.type = type;
        this.power = power;
        this.isActive = true;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
