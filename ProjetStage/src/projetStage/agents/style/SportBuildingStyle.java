package projetStage.agents.style;

import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceShape;

import java.awt.Color;

import projetStage.agents.SportBuilding;

import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

public class SportBuildingStyle implements SurfaceShapeStyle<SportBuilding>{

	@Override
	public SurfaceShape getSurfaceShape(SportBuilding sportBuilding, SurfaceShape shape) {
        if (shape == null)
            return new SurfacePolygon();

        return shape;
	}

	@Override
	public Color getFillColor(SportBuilding sportBuilding) {
		return Color.PINK;
	}

	@Override
	public double getFillOpacity(SportBuilding sportBuilding) {
		return 0.50;
	}

	@Override
	public Color getLineColor(SportBuilding sportBuilding) {
		return Color.PINK;
	}

	@Override
	public double getLineOpacity(SportBuilding sportBuilding) {
		return 0.5;
	}

	@Override
	public double getLineWidth(SportBuilding sportBuilding) {
		return 0.1;
	}
}
