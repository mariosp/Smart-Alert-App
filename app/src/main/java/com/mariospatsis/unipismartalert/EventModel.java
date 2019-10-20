package com.mariospatsis.unipismartalert;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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

    public static List<EventModel> filterEarthquakeDetectionEvents(List<EventModel> eventModels){
        //Επιστρεφουμε μονο τα events που ειναι earthquakeDetection απο την λιστα
        List<EventModel> result = new ArrayList<EventModel>();
        for(EventModel event: eventModels ){
            if(event.type.equals("earthquakeDetection")){
                result.add(event);
            }
        }

        return result;
    }
}
