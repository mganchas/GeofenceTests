package com.example.x190629.testes_geofence.entities;

import android.support.annotation.NonNull;

import com.example.x190629.testes_geofence.entities.GeoArea;

import java.io.Serializable;
import java.util.List;

/**
 * Created by X190629 on 11/07/2019.
 */

public class Airport implements Serializable
{
    private final String name;
    private final List<GeoArea> fence;

    public Airport(@NonNull String name, @NonNull List<GeoArea> fence)
    {
        this.name = name;
        this.fence = fence;
    }

    public String getName() {
        return name;
    }

    public List<GeoArea> getFence() {
        return fence;
    }

    public void addGeoArea(@NonNull GeoArea geoArea) {
        this.fence.add(geoArea);
    }
}
