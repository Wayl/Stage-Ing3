package projetStage.agents.style;

import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceShape;

import java.awt.Color;

import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

import projetStage.agents.Microgrid;

public class MicrogridStyle implements SurfaceShapeStyle<Microgrid>{

	@Override
	public SurfaceShape getSurfaceShape(Microgrid object, SurfaceShape shape) {
		return new SurfacePolygon();
	}

	@Override
	public Color getFillColor(Microgrid zone) {
		return Color.WHITE;
	}

	@Override
	public double getFillOpacity(Microgrid obj) {
		return 0.3;
	}

	@Override
	public Color getLineColor(Microgrid zone) {
		return Color.BLUE;
	}

	@Override
	public double getLineOpacity(Microgrid obj) {
		return 0.6;
	}

	@Override
	public double getLineWidth(Microgrid obj) {
		return 0.5;
	}
}
