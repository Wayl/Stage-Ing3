package projetStage.agents.building;

import com.vividsolutions.jts.geom.Geometry;

import java.util.Map;

/**
 * Classe représentant un batiment
 */
public class Building implements Comparable<Building> {
    private String id;          // Id du batiment
    private String nature;      // Nature du batiment
    private Geometry geom;      // Forme géométrique
    private double distance;    // Distance par rapport au centre de la microgrid dans laquelle il est contenu (utilisé uniquement pendant l'initialisation)
    private Map<String, Double> mapConso;   // Profil de consommation

    /**
     * Constructeur
     *
     * @param id     id
     * @param nature nature
     * @param geom   forme géométrique
     */
    public Building(String id, String nature, Geometry geom) {
        super();
        this.id = id;
        this.nature = nature;
        this.geom = geom;
        this.distance = 0;
    }

    public String getId() {
        return id;
    }

    public String getNature() {
        return nature;
    }

    public Geometry getGeometry() {
        return geom;
    }

    public void setGeometry(Geometry geom) {
        this.geom = geom;
    }

    public double distance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Double getConso(String date) {
        // Valeurs totalement arbitraires pour que la consommation globale soit proportionnelle à la
        // production globale...
        Double random = Math.random() * 0.3 + 0.85;
        return mapConso.get(date) * random * 25;
    }

    public void setMapConso(Map<String, Double> mapConso) {
        this.mapConso = mapConso;
    }

    /**
     * Permet de comparer 2 batiments en fonction de leur distance au centre de la microgrid
     *
     * @param building Batiment avec lequel il faut effectuer la comparaison
     *
     * @return Retourne 0 si les 2 batiments sont à égale distance. Retourne 1 si this est le plus
     * proche, -1 si building est le plus proche
     */
    @Override
    public int compareTo(Building building) {
        double dist = building.distance() - this.distance();
        if (dist > 0)
            return 1;
        if (dist < 0)
            return -1;
        return 0;
    }
}
