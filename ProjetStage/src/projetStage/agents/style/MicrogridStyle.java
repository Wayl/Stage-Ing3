package projetStage.agents.style;

import gov.nasa.worldwind.render.SurfacePolyline;
import gov.nasa.worldwind.render.SurfaceShape;

import java.awt.Color;

import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

import projetStage.agents.Microgrid;

public class MicrogridStyle implements SurfaceShapeStyle<Microgrid>{

	@Override
	public SurfaceShape getSurfaceShape(Microgrid object, SurfaceShape shape) {
		return new SurfacePolyline();
	}

	@Override
	public Color getFillColor(Microgrid zone) {
		return null;
	}

	@Override
	public double getFillOpacity(Microgrid obj) {
		return 0;
	}

	@Override
	public Color getLineColor(Microgrid zone) {
		return Color.BLUE;
	}

	@Override
	public double getLineOpacity(Microgrid obj) {
		return 1;
	}

	@Override
	public double getLineWidth(Microgrid obj) {
		return 0.5;
	}
}
