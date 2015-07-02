package projetStage.agents.style;

import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceShape;

import java.awt.Color;

import projetStage.agents.NoticeableBuilding;

import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

public class NoticeableBuildingStyle implements SurfaceShapeStyle<NoticeableBuilding>{

	@Override
	public SurfaceShape getSurfaceShape(NoticeableBuilding object, SurfaceShape shape) {
		return new SurfacePolygon();
	}

	@Override
	public Color getFillColor(NoticeableBuilding zone) {
		return Color.WHITE;
	}

	@Override
	public double getFillOpacity(NoticeableBuilding obj) {
		return 0.50;
	}

	@Override
	public Color getLineColor(NoticeableBuilding zone) {
		return Color.WHITE;
	}

	@Override
	public double getLineOpacity(NoticeableBuilding obj) {
		return 0.5;
	}

	@Override
	public double getLineWidth(NoticeableBuilding obj) {
		return 0.1;
	}
}
