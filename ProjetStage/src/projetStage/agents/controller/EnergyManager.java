package projetStage.agents.controller;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import projetStage.ContextCreator;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;

import java.io.*;
import java.util.*;

/**
 * Created by wayl on 17/09/15 !
 */
public class EnergyManager {
    private static Map<String, Producer> mapProducer;
    private Map<Coordinate, List<String>> mapSortedProducer;

    private int nbGridDown;

    /**
     * Constructeur par défaut
     * Initialisation
     */
    public EnergyManager(Map<Coordinate, List<String>> map) {
        mapProducer = new HashMap<>();
        mapSortedProducer = map;
    }


    /**
     * Méthode effectuée à chaque step
     */
    @ScheduledMethod(start = 0, interval = 1, priority = 3)
    public void step() {
        nbGridDown = 0;
    }


    /**
     * Une microgrid demande de l'énergie
     * On recherche quel est le générateur le plus proche qui peut fournir de l'énergie
     *
     * @param e énergie demandée
     * @param coord coordonnée de la microgrid
     *
     * @return 0 si toute l'énergie a été allouée, sinon l'energie qui n'a pas pu être allouée
     */
    public double allocateEnergy(double e, Coordinate coord) {
        double energyTmp = e;

        for(String prod : mapSortedProducer.get(ContextCreator.hashForGrid(coord))) {
            Producer producer = mapProducer.get(prod);
            if(producer.isActive() && producer.getPowerAvailable() > 0) {
                energyTmp -= producer.allocate(energyTmp);
            }
            if(energyTmp <= 0)
                return 0;
        }

        if(energyTmp > 0) ++nbGridDown;

        return energyTmp;
    }


    /**
     * Création des producteurs d'électricité
     *
     * @param context   context
     * @param geography geography
     */
    public void createProducers(final Context<Object> context, final Geography<Object> geography) {
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
                Coordinate coord = new Coordinate(Double.parseDouble(x + 2), Double.parseDouble(y + 2));
                Producer prod = new Producer(name, type, Double.parseDouble(power), coord);

                mapProducer.put(prod.getName(), prod);
                context.add(prod);
                Coordinate[] coordinates = new Coordinate[1];
                coordinates[0] = coord;
                geography.move(prod, new Point(new CoordinateArraySequence(coordinates), new GeometryFactory()));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Initialise producerMap
     * Les clés de la map représentent les coordonnées d'une case de la grille
     * Les valeurs sont la liste des producteurs classés par ordre de proximité à la case de la grille
     */
    public void initProducerMap() {
        for (Coordinate coord : mapSortedProducer.keySet()) {
            Map<Double, String> map = new HashMap<>();
            // On récupère la liste des distances et on la trie
            for (String prod : mapProducer.keySet()) {
                Coordinate coorProd = new Coordinate();
                coorProd.setCoordinate(mapProducer.get(prod).getCoord());
                coorProd.x = coorProd.x * 1000;
                coorProd.y = coorProd.y * 1000;
                map.put(coord.distance(coorProd), mapProducer.get(prod).getName());
            }
            List<Double> sortedList = Arrays.asList(map.keySet().toArray(new Double[mapProducer.size()]));
            Collections.sort(sortedList);
//            Collections.reverse(sortedList);

            // On insère les producteurs triés par distance dans la map
            for (Double dist : sortedList) {
                mapSortedProducer.get(coord).add(map.get(dist));
            }
        }
    }


    public int getNbGridDown() {
        return nbGridDown;
    }
}
