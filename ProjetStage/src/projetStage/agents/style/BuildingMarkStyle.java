package projetStage.agents.style;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.*;
import projetStage.agents.building.Mark;
import repast.simphony.visualization.gis3D.PlaceMark;
import repast.simphony.visualization.gis3D.style.MarkStyle;

import java.awt.*;
import java.net.URL;

/**
 * Created by wayl on 15/07/15 !
 */
public class BuildingMarkStyle implements MarkStyle<Mark> {
    private final Offset labelOffset;
    private final Offset iconOffset;
    private final BasicWWTexture iconEclair;
    private final BasicWWTexture iconEclairRouge;

    /**
     * Constructeur par défaut
     */
    public BuildingMarkStyle() {
        // Initialisation offset
        labelOffset = new Offset(-0.7d, -0.7d, AVKey.FRACTION, AVKey.FRACTION);
        iconOffset = new Offset(20.0, 20.0, AVKey.PIXELS, AVKey.PIXELS);

        // Initialisation icons
        URL iconEclairUrl = WorldWind.getDataFileStore().requestFile("icons/eclair.png");
        URL iconEclairRougeUrl = WorldWind.getDataFileStore().requestFile("icons/eclairRouge.png");
        if (iconEclairUrl != null) {
            iconEclair = new BasicWWTexture(iconEclairUrl, false);
        } else {
            iconEclair = null;
        }
        if (iconEclairRougeUrl != null) {
            iconEclairRouge = new BasicWWTexture(iconEclairRougeUrl, false);
        } else {
            iconEclairRouge = null;
        }
    }


    @Override
    public WWTexture getTexture(Mark mark, WWTexture texture) {
        if (mark.isPowerOn())
            return iconEclair;
        else
            return iconEclairRouge;
        //BufferedImage image imgimgur= PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, new Dimension(5, 5), 0.7f, color);
    }

    @Override
    public PlaceMark getPlaceMark(Mark mark, PlaceMark placeMark) {
        if(placeMark == null) {
            PlaceMark place = new PlaceMark();
            place.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            place.setLineEnabled(false);
            return place;
        }
        return placeMark;
    }

    @Override
    public Offset getIconOffset(Mark mark) {
        return iconOffset;
    }

    @Override
    public double getElevation(Mark mark) {
        return 50;
    }

    @Override
    public double getScale(Mark mark) {
        if (mark.isPowerOn()) {
            return 0.2;
        } else {
            return 0.3;
        }
    }

    @Override
    public double getHeading(Mark mark) {
        return 0;
    }

    @Override
    public String getLabel(Mark mark) {
        return Integer.toString(mark.getNbBuilding());
    }

    @Override
    public Color getLabelColor(Mark mark) {
        if (mark.isPowerOn()) {
            return Color.BLACK;
        } else {
            return Color.RED;
        }
    }

    @Override
    public Font getLabelFont(Mark mark) {
        return null;
    }

    @Override
    public Offset getLabelOffset(Mark mark) {
        return labelOffset;
    }

    @Override
    public double getLineWidth(Mark mark) {
        if (mark.isPowerOn()) {
            return 0.7;
        } else {
            return 1;
        }
    }

    @Override
    public Material getLineMaterial(Mark mark, Material lineMaterial) {
        if (lineMaterial == null) {
            lineMaterial = new Material(Color.BLACK);
        }

        return lineMaterial;
    }
}
