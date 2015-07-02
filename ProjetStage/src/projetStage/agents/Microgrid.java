package projetStage.agents;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;

public class Microgrid {
	private List<Building> buidingList = new ArrayList<>();

	public Microgrid(Context<Object> context, Geography<Object> geography, List<Building> featureList) {
		Geography<Object> geo = (Geography<Object>)context.getProjection("Geography");
		GeometryFactory fac = new GeometryFactory();

		Geometry centerList = new Polygon(null, null, fac);
		
		for(Building building : featureList) {
			Geometry geom = building.getGeometry();
			/*buidingList.add(building);
			context.add(building);
			geo.move(building, geom);*/

			centerList = centerList.union(geom.getCentroid());
		}

		Geometry convexHull = centerList.convexHull();
		Coordinate[] coordinateArray = convexHull.getCoordinates();

		if(coordinateArray.length >= 2) {
			LineString line = new LineString(new CoordinateArraySequence(coordinateArray), fac);
			context.add(line);
			geography.move(this, line);
		} else {
			context.add(featureList.get(0));
			geo.move(featureList.get(0), featureList.get(0).getGeometry());
		}
	}
}
