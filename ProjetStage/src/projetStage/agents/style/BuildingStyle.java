package projetStage.agents.style;

import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceShape;

import java.awt.Color;

import projetStage.agents.Building;

import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

public class BuildingStyle implements SurfaceShapeStyle<Building>{

	@Override
	public SurfaceShape getSurfaceShape(Building building, SurfaceShape shape) {
        if(shape == null)
		    return new SurfacePolygon();

        return shape;
	}

	@Override
	public Color getFillColor(Building building) {
		return Color.BLACK;
	}

	@Override
	public double getFillOpacity(Building building) {
		return 0.50;
	}

	@Override
	public Color getLineColor(Building building) {
		return Color.BLACK;
	}

	@Override
	public double getLineOpacity(Building building) {
		return 0.5;
	}

	@Override
	public double getLineWidth(Building building) {
		return 0.1;
	}
}
