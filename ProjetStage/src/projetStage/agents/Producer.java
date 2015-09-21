package projetStage.agents;

import com.vividsolutions.jts.geom.Coordinate;
import repast.simphony.engine.schedule.ScheduledMethod;

/**
 * Created by wayl on 19/08/15 !
 */
public class Producer {
    private String name;
    private String type;
    private double powerMax;
    private double powerUsed;
    private boolean active;
    private Coordinate coord;


    /**
     * Constructeur par défaut
     *
     * @param name     Nom du producteur
     * @param type     Type d'énergie utilisée
     * @param powerMax Puissance maximale du producteur en MW
     */
    public Producer(String name, String type, double powerMax, Coordinate coord) {
        this.name = name;
        this.type = type;
        this.powerMax = powerMax * 1000000;
        this.powerUsed = 0;
        this.active = true;
        this.coord = coord;
    }


    /**
     * Méthode effectuée à chaque step
     * <p/>
     * Réitialisation de la production
     */
    @ScheduledMethod(start = 0, interval = 1, priority = 2)
    public void step() {
        powerUsed = 0;
    }


    /**
     * Demande d'allocation d'une quantité d'energie
     *
     * @param energy energie demandée
     *
     * @return l'énergie allouée (return 0 <=> reacteur au maximum de sa capacité, ne peut pas allouer de l'energie)
     */
    public double allocate(double energy) {
        double available = getPowerAvailable();
        if (available >= energy) {
            setPowerUsed(powerUsed + energy);
            return energy;
        } else {
            setPowerUsed(powerUsed + available);
            return available;
        }
    }


    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getPowerMax() {
        if (isActive())
            return powerMax;
        else
            return 0;
    }

    public double getPowerUsed() {
        return powerUsed;
    }

    public void setPowerUsed(double powerUsed) {
        if (powerUsed > powerMax)
            this.powerUsed = powerMax;
        else if (powerUsed < 0)
            this.powerUsed = 0;
        else
            this.powerUsed = powerUsed;
    }

    public double getPowerAvailable() {
        return powerMax - powerUsed;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Coordinate getCoord() {
        return coord;
    }
}
