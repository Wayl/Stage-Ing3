package projetStage.agents.style;

import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceShape;

import java.awt.Color;

import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

import projetStage.agents.Microgrid;

public class MicrogridStyle implements SurfaceShapeStyle<Microgrid>{

	@Override
	public SurfaceShape getSurfaceShape(Microgrid microgrid, SurfaceShape shape) {
        if (shape == null)
            return new SurfacePolygon();

        return shape;
	}

	@Override
	public Color getFillColor(Microgrid microgrid) {
		return Color.WHITE;
	}

	@Override
	public double getFillOpacity(Microgrid microgrid) {
		return 0.3;
	}

	@Override
	public Color getLineColor(Microgrid microgrid) {
		return Color.BLUE;
	}

	@Override
	public double getLineOpacity(Microgrid microgrid) {
		return 0.6;
	}

	@Override
	public double getLineWidth(Microgrid microgrid) {
		return 0.5;
	}
}
