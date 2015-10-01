package projetStage.agents.building;

import com.vividsolutions.jts.geom.Geometry;

import java.util.Map;

public class Building implements Comparable<Building> {
    private String id;
	private String nature;
	private Geometry geom;
	private double distance;
    private Map<String, Double> mapConso;

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

	public String getNature() {
		return nature;
	}

	public Geometry getGeometry() {
		return geom;
	}

	public void setGeometry(Geometry geom) {
		this.geom = geom;
	}

    public double distance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Double getConso(String date) {
        Double random = Math.random()*0.3 + 0.85;
        return mapConso.get(date)*random * 25;
    }

    public void setMapConso(Map<String, Double> mapConso) {
        this.mapConso = mapConso;
    }

    @Override
    public int compareTo(Building building) {
        double dist = building.distance() - this.distance();
        if(dist > 0)
            return 1;
        if(dist < 0)
            return -1;
        return 0;
    }
}
