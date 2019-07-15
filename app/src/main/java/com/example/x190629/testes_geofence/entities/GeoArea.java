package com.example.x190629.testes_geofence.entities;

/**
 * Created by X190629 on 11/07/2019.
 */

public class GeoArea
{
    private final double latitude;
    private final double longitude;
    private final float radius;

    public GeoArea(double latitude, double longitude, float radius)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public float getRadius() {
        return radius;
    }
}
