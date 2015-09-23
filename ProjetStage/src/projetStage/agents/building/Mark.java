package projetStage.agents.building;

/**
 * Affiche les nombre de batiments et une icone indiquant si la microgride est alimentée en électricité
 *
 * Created by wayl on 15/07/15 !
 */
public class Mark {
    private int nbBuilding;
    private boolean powerOn = true;

    public Mark(int nbBuilding) {
        this.nbBuilding = nbBuilding;
    }

    public int getNbBuilding() {
        return nbBuilding;
    }

    public void setNbBuilding(int nbBuilding) {
        this.nbBuilding = nbBuilding;
    }

    public boolean isPowerOn() {
        return powerOn;
    }

    public void setPowerOn(boolean powerOn) {
        this.powerOn = powerOn;
    }
}
