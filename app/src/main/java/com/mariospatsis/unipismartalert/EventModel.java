package com.mariospatsis.unipismartalert;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class EventModel {
    public String type;
    public double lat;
    public double lon;
    public String city;
    public long timestamp;

    public EventModel(){}
    public EventModel(String type, double lat, double lon, String city, long timestamp) {
        this.type = type;
        this.lat = lat;
        this.lon = lon;
        this.city = city;
        this.timestamp = timestamp;
    }
}
