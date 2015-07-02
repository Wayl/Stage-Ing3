package projetStage.agents.style;

import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceShape;

import java.awt.Color;

import projetStage.agents.SportBuilding;

import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

public class SportBuildingStyle implements SurfaceShapeStyle<SportBuilding>{

	@Override
	public SurfaceShape getSurfaceShape(SportBuilding object, SurfaceShape shape) {
		return new SurfacePolygon();
	}

	@Override
	public Color getFillColor(SportBuilding zone) {
		return Color.PINK;
	}

	@Override
	public double getFillOpacity(SportBuilding obj) {
		return 0.50;
	}

	@Override
	public Color getLineColor(SportBuilding zone) {
		return Color.PINK;
	}

	@Override
	public double getLineOpacity(SportBuilding obj) {
		return 0.5;
	}

	@Override
	public double getLineWidth(SportBuilding obj) {
		return 0.1;
	}
}
