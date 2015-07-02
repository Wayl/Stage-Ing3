package projetStage.agents;

import com.vividsolutions.jts.geom.Geometry;

public class Building {
	private String id;
	private String nature;
	private Geometry geom;

	public Building(String id, String nature, Geometry geom) {
		super();
		this.id = id;
		this.nature = nature;
		this.geom = geom;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNature() {
		return nature;
	}

	public void setNature(String nature) {
		this.nature = nature;
	}

	public Geometry getGeometry() {
		return geom;
	}

	public void setGeometry(Geometry geom) {
		this.geom = geom;
	}
}
