package com.example.x190629.testes_geofence.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by X191104 on 7/16/2019.
 */

public class PointsOfInterest
{
    private static final String BCP_KEY = "BCP";
    public static List<Airport> airports;

    static
    {
        if (airports == null) {
            airports = getPointsOfInterest();
        }
    }

    private static List<Airport> getPointsOfInterest()
    {
        List<Airport> points = new ArrayList<>();
        List<GeoArea> geoFence = new ArrayList<>();
        Airport airport = null;

        // beja
        geoFence.add(new GeoArea(38.079048, -7.925615, 2000.00f));
        airport = new Airport("Beja", geoFence);
        points.add(airport);

        geoFence.clear();
        // humberto delgado
        geoFence.add(new GeoArea(38.765486, -9.142942, 225.36f));
        geoFence.add(new GeoArea(38.769585, -9.139723, 305.45f));
        geoFence.add(new GeoArea(38.767427, -9.133200, 270.06f));
        geoFence.add(new GeoArea(38.769501, -9.128865, 158.31f));
        geoFence.add(new GeoArea(38.775058, -9.133049, 545.70f));
        geoFence.add(new GeoArea(38.782845, -9.134198, 188.22f));
        geoFence.add(new GeoArea(38.786811, -9.133851, 240.36f));
        geoFence.add(new GeoArea(38.790781, -9.131366, 240.36f));
        geoFence.add(new GeoArea(38.794769, -9.129276, 240.36f));
        airport = new Airport("Humberto Delgado", geoFence);
        points.add(airport);

        return points;
    }

    public static void removeBCP()
    {
        for (Airport airport: airports)
        {
            if (airport.getName().equals(BCP_KEY)) {
                airports.remove(airport);
                break;
            }
        }
    }

    public static void addBCP()
    {
        boolean exists = false;
        for (Airport airport: airports)
        {
            if (airport.getName().equals(BCP_KEY)) {
                exists = true;
                break;
            }
        }

        if (!exists)
        {
            List<GeoArea> fence = new ArrayList<>();
            fence.add(new GeoArea(38.743919, -9.306373, 100));
            airports.add(new Airport(BCP_KEY, fence));
        }
    }
}
