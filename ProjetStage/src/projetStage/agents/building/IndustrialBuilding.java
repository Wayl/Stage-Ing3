package projetStage.agents.building;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Batiment industriel
 */
public class IndustrialBuilding extends Building {
    public IndustrialBuilding(String id, String nature, Geometry geom) {
        super(id, nature, geom);
    }
}
