package projetStage.agents.style;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.*;
import projetStage.agents.Producer;
import repast.simphony.visualization.gis3D.PlaceMark;
import repast.simphony.visualization.gis3D.style.MarkStyle;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by wayl on 20/08/15 !
 */
public class ProducerStyle implements MarkStyle<Producer> {
    private final Offset labelOffset;
    private double powerUsed=-1;

    public ProducerStyle() {
        labelOffset = new Offset(-8d, -1.7d, AVKey.FRACTION, AVKey.FRACTION);
    }


    @Override
    public WWTexture getTexture(Producer producer, WWTexture texture) {
        if (texture == null || powerUsed != producer.getPowerUsed()) {
            BufferedImage image;
            powerUsed = producer.getPowerUsed();
            int power = (int)Math.floor(255 * (producer.getPowerUsed() / producer.getPowerMax()));
            if(power > 255) power = 255;
            if(power < 0) power = 0;
            Color color = new Color(power, 255 - power, 0);
            int dim = (int)(producer.getPowerMax() / 20000000) + 5;
            image = PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, new Dimension(dim, dim), 1, color);
            return new BasicWWTexture(image);
        }
        return texture;
    }

    @Override
    public PlaceMark getPlaceMark(Producer producer, PlaceMark placeMark) {
        if (placeMark == null) {
            PlaceMark place = new PlaceMark();
            place.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            place.setLineEnabled(false);
            return place;
        }
        return placeMark;
    }

    @Override
    public Offset getIconOffset(Producer producer) {
        //return new Offset(10d, -10d, AVKey.PIXELS, AVKey.PIXELS);
        return null;
    }

    @Override
    public double getElevation(Producer producer) {
        return 200;
    }

    @Override
    public double getScale(Producer producer) {
        return 1;
    }

    @Override
    public double getHeading(Producer producer) {
        return 0;
    }

    @Override
    public String getLabel(Producer producer) {
        return producer.getName();
    }

    @Override
    public Color getLabelColor(Producer producer) {
        if (producer.isActive()) {
            return Color.BLACK;
        } else {
            return Color.RED;
        }
    }

    @Override
    public Font getLabelFont(Producer producer) {
        return null;
    }

    @Override
    public Offset getLabelOffset(Producer producer) {
        return labelOffset;
    }

    @Override
    public double getLineWidth(Producer producer) {
        if (producer.isActive()) {
            return 0.7;
        } else {
            return 1;
        }
    }

    @Override
    public Material getLineMaterial(Producer producer, Material lineMaterial) {
        if (lineMaterial == null) {
            lineMaterial = new Material(Color.WHITE);
        }

        return lineMaterial;
    }
}



/*public class ProducerStyle implements SurfaceShapeStyle<Producer> {

    @Override
    public SurfaceShape getSurfaceShape(Producer producer, SurfaceShape shape) {
        if (shape == null) {
            return new SurfaceCircle(LatLon.fromRadians(55.55002, -20.90332), 20);
        }

        return shape;
    }

    @Override
    public Color getFillColor(Producer producer) {
        return Color.RED;
    }

    @Override
    public double getFillOpacity(Producer producer) {
        return 0.70;
    }

    @Override
    public Color getLineColor(Producer producer) {
        return Color.BLACK;
    }

    @Override
    public double getLineOpacity(Producer producer) {
        return 1;
    }

    @Override
    public double getLineWidth(Producer producer) {
        return 0.2;
    }
}*/
