package projetStage.agents.building;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Batiment remarquable (batiments officiels, religieux, ...)
 */
public class NoticeableBuilding extends Building {
    public NoticeableBuilding(String id, String nature, Geometry geom) {
        super(id, nature, geom);
    }
}
