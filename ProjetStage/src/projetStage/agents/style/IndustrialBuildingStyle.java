package projetStage.agents.style;

import projetStage.agents.IndustrialBuilding;

import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceShape;

import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

import java.awt.Color;

public class IndustrialBuildingStyle implements SurfaceShapeStyle<IndustrialBuilding> {
    @Override
    public SurfaceShape getSurfaceShape(IndustrialBuilding industrialBuilding, SurfaceShape shape) {
        if (shape == null)
            return new SurfacePolygon();

        return shape;
    }

    @Override
    public Color getFillColor(IndustrialBuilding industrialBuilding) {
        return Color.YELLOW;
    }

    @Override
    public double getFillOpacity(IndustrialBuilding industrialBuilding) {
        return 0.50;
    }

    @Override
    public Color getLineColor(IndustrialBuilding industrialBuilding) {
        return Color.YELLOW;
    }

    @Override
    public double getLineOpacity(IndustrialBuilding industrialBuilding) {
        return 0.5;
    }

    @Override
    public double getLineWidth(IndustrialBuilding industrialBuilding) {
        return 0.1;
    }
}
