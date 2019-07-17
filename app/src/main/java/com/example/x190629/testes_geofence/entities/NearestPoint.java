package com.example.x190629.testes_geofence.entities;

import java.util.List;

/**
 * Created by X190629 on 12/07/2019.
 */

public class NearestPoint
{
    private final Airport airport;
    private final int geoAreaIndex;
    private final float distance;

    public NearestPoint(Airport airport, int geoAreaIndex, float distance) {
        this.airport = airport;
        this.geoAreaIndex = geoAreaIndex;
        this.distance = distance;
    }

    public GeoArea getGeoArea() {
        return airport.getFence().get(geoAreaIndex);
    }

    public String getAirportName() {
        return airport.getName();
    }

    public List<GeoArea> getAirportFence() {
        return airport.getFence();
    }

    public float getDistance() {
        return distance;
    }
}
