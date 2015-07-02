package projetStage.agents.style;

import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceShape;

import java.awt.Color;

import projetStage.agents.UndifferentiatedBuilding;

import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

public class UndifferentiatedBuildingStyle implements SurfaceShapeStyle<UndifferentiatedBuilding>{

	@Override
	public SurfaceShape getSurfaceShape(UndifferentiatedBuilding object, SurfaceShape shape) {
		return new SurfacePolygon();
	}

	@Override
	public Color getFillColor(UndifferentiatedBuilding zone) {
		return Color.GREEN;
	}

	@Override
	public double getFillOpacity(UndifferentiatedBuilding obj) {
		return 0.50;
	}

	@Override
	public Color getLineColor(UndifferentiatedBuilding zone) {
		return Color.GREEN;
	}

	@Override
	public double getLineOpacity(UndifferentiatedBuilding obj) {
		return 0.5;
	}

	@Override
	public double getLineWidth(UndifferentiatedBuilding obj) {
		return 0.1;
	}
}
