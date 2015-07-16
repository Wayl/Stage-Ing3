package projetStage.agents;

/**
 * Created by wayl on 15/07/15 !
 */
public class Mark {
    private String label;
    private boolean powerOn = true;

    public Mark(String label) {
        this.label = label;
        if(Math.random() > 0.5)
            powerOn = false;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isPowerOn() {
        return powerOn;
    }

    public void setPowerOn(boolean powerOn) {
        this.powerOn = powerOn;
    }
}
