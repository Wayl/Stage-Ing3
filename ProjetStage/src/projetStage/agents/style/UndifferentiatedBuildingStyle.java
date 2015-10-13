package projetStage.agents.style;

import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceShape;

import java.awt.Color;

import projetStage.agents.building.UndifferentiatedBuilding;

import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

public class UndifferentiatedBuildingStyle implements SurfaceShapeStyle<UndifferentiatedBuilding> {

    @Override
    public SurfaceShape getSurfaceShape(UndifferentiatedBuilding undifferentiatedBuilding, SurfaceShape shape) {
        if (shape == null)
            return new SurfacePolygon();

        return shape;
    }

    @Override
    public Color getFillColor(UndifferentiatedBuilding undifferentiatedBuilding) {
        return Color.GREEN;
    }

    @Override
    public double getFillOpacity(UndifferentiatedBuilding undifferentiatedBuilding) {
        return 0.50;
    }

    @Override
    public Color getLineColor(UndifferentiatedBuilding undifferentiatedBuilding) {
        return Color.GREEN;
    }

    @Override
    public double getLineOpacity(UndifferentiatedBuilding undifferentiatedBuilding) {
        return 0.5;
    }

    @Override
    public double getLineWidth(UndifferentiatedBuilding undifferentiatedBuilding) {
        return 0.1;
    }
}
