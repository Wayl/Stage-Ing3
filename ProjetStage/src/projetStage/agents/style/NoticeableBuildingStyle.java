package projetStage.agents.style;

import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceShape;

import java.awt.Color;

import projetStage.agents.building.NoticeableBuilding;

import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

public class NoticeableBuildingStyle implements SurfaceShapeStyle<NoticeableBuilding> {

    @Override
    public SurfaceShape getSurfaceShape(NoticeableBuilding noticeableBuilding, SurfaceShape shape) {
        if (shape == null)
            return new SurfacePolygon();

        return shape;
    }

    @Override
    public Color getFillColor(NoticeableBuilding noticeableBuilding) {
        return Color.WHITE;
    }

    @Override
    public double getFillOpacity(NoticeableBuilding noticeableBuilding) {
        return 0.50;
    }

    @Override
    public Color getLineColor(NoticeableBuilding noticeableBuilding) {
        return Color.WHITE;
    }

    @Override
    public double getLineOpacity(NoticeableBuilding noticeableBuilding) {
        return 0.5;
    }

    @Override
    public double getLineWidth(NoticeableBuilding noticeableBuilding) {
        return 0.1;
    }
}
