package projetStage.agents.building;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Batiment indifférencié (résidentiels ou non défini)
 */
public class UndifferentiatedBuilding extends Building {
    public UndifferentiatedBuilding(String id, String nature, Geometry geom) {
        super(id, nature, geom);
    }
}
