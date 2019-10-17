package com.mariospatsis.unipismartalert;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class EventModel {
    public String type;
    public double lat;
    public double lon;
    public long timestamp;

    public EventModel(){}
    public EventModel(String type, double lat, double lon, long timestamp) {
        this.type = type;
        this.lat = lat;
        this.lon = lon;
        this.timestamp = timestamp;
    }
}
