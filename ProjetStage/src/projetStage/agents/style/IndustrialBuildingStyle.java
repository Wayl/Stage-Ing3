package projetStage.agents.style;

import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceShape;

import java.awt.Color;

import projetStage.agents.IndustrialBuilding;

import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

public class IndustrialBuildingStyle implements SurfaceShapeStyle<IndustrialBuilding>{

	@Override
	public SurfaceShape getSurfaceShape(IndustrialBuilding object, SurfaceShape shape) {
		return new SurfacePolygon();
	}

	@Override
	public Color getFillColor(IndustrialBuilding zone) {
		return Color.YELLOW;
	}

	@Override
	public double getFillOpacity(IndustrialBuilding obj) {
		return 0.50;
	}

	@Override
	public Color getLineColor(IndustrialBuilding zone) {
		return Color.YELLOW;
	}

	@Override
	public double getLineOpacity(IndustrialBuilding obj) {
		return 0.5;
	}

	@Override
	public double getLineWidth(IndustrialBuilding obj) {
		return 0.1;
	}
}
