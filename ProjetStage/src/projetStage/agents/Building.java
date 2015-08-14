package projetStage.agents;

import com.vividsolutions.jts.geom.Geometry;

public class Building implements Comparable<Building> {
	private String id;
	private String nature;
	private Geometry geom;
	private double distance;

	public Building(String id, String nature, Geometry geom) {
		super();
		this.id = id;
		this.nature = nature;
		this.geom = geom;
        this.distance = 0;
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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public int compareTo(Building building) {
        double dist = building.getDistance() - this.getDistance();
        if(dist > 0)
            return 1;
        if(dist < 0)
            return -1;
        return 0;
    }
}
