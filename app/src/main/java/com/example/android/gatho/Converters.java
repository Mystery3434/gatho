package com.example.android.gatho;

import android.arch.persistence.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;

public  class Converters {
    @TypeConverter
    public static String fromLatLng(LatLng latlng) {
        return latlng.latitude + "," + latlng.longitude;
    }

    @TypeConverter
    public static LatLng stringToLatlng(String latlng_str) {
        String[] coordinates = latlng_str.replace("(","").replace(")","").split(",");
        return  new LatLng( Double.parseDouble(coordinates[0]),
                            Double.parseDouble(coordinates[1])
                            );
    }
}
