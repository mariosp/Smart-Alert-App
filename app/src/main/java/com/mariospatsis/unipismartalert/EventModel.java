package com.mariospatsis.unipismartalert;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/* EventModel class
* To μοντελο των events */
@IgnoreExtraProperties
public class EventModel {
    public String type; // ονομα του event
    public double lat; // latitude
    public double lon; //longitude
    public long timestamp; // timestamp toy event

    public EventModel(){}
    public EventModel(String type, double lat, double lon, long timestamp) {
        this.type = type;
        this.lat = lat;
        this.lon = lon;
        this.timestamp = timestamp;
    }

    /*
    * filterEarthquakeDetectionEvents
    * H στατικη μεθοδος δεχεται μια λιστα απο αντικειμενα EventModel
    * και επιστρεφει λιστα απο events που εχουν type=earthquakeDetection
    * Χρησιμοποιειται για να φιλτραρει τα events  */
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
