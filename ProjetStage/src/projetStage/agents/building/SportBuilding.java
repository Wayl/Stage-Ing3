package projetStage.agents.building;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Batiment et installation sportive
 */
public class SportBuilding extends Building {
    public SportBuilding(String id, String nature, Geometry geom) {
        super(id, nature, geom);
    }
}
