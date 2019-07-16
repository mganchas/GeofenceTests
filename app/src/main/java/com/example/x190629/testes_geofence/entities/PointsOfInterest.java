package com.example.x190629.testes_geofence.entities;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by X191104 on 7/16/2019.
 */

public class PointsOfInterest {

    public static Map<String, GeoArea> pointsOfInterest = new HashMap();

    static {
        if (pointsOfInterest == null || pointsOfInterest.isEmpty()) {
            pointsOfInterest = getPointsOfInterest();
        }
    }

    private static Map<String, GeoArea> getPointsOfInterest() {
        Map<String, GeoArea> points = new HashMap<>();

        // beja
        points.put("Beja", new GeoArea(38.079048, -7.925615, 2000.00f));

        // humberto delgado
        points.put("Humberto Delgado", new GeoArea(38.765486, -9.142942, 225.36f));
        points.put("Humberto Delgado", new GeoArea(38.769585, -9.139723, 305.45f));
        points.put("Humberto Delgado", new GeoArea(38.767427, -9.133200, 270.06f));
        points.put("Humberto Delgado", new GeoArea(38.769501, -9.128865, 158.31f));
        points.put("Humberto Delgado", new GeoArea(38.775058, -9.133049, 545.70f));
        points.put("Humberto Delgado", new GeoArea(38.782845, -9.134198, 188.22f));
        points.put("Humberto Delgado", new GeoArea(38.786811, -9.133851, 240.36f));
        points.put("Humberto Delgado", new GeoArea(38.790781, -9.131366, 240.36f));
        points.put("Humberto Delgado", new GeoArea(38.794769, -9.129276, 240.36f));

        // bcp edificio 9
        points.put("BCP", new GeoArea(38.743919, -9.306373, 10));

        return points;
    }
}
