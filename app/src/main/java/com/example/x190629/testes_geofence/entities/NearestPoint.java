package com.example.x190629.testes_geofence.entities;

/**
 * Created by X190629 on 12/07/2019.
 */

public class NearestPoint
{
    private GeoArea geoArea;
    private float distance;


    public NearestPoint(GeoArea geoArea, float distance) {
        this.geoArea = geoArea;
        this.distance = distance;
    }

    public GeoArea getGeoArea() {
        return geoArea;
    }

    public float getDistance() {
        return distance;
    }
}
