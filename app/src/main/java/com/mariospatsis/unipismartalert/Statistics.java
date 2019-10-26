package com.mariospatsis.unipismartalert;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Statistics extends AppCompatActivity implements AdapterView.OnItemSelectedListener  {
    FirebaseService mFirebaseService = FirebaseService.getInstance();
    Spinner spinner;
    TextView count;
    ListView listView;
    List<EventModel> events;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        count = findViewById(R.id.s_count);
        listView = findViewById(R.id.listview);

        mFirebaseService.getUserEvents();


        mFirebaseService.setFirebaseListener(new FirebaseListener() {
            @Override
            public void onStatusChanged(String newStatus) {
                if(newStatus == "userEvents"){
                    events = mFirebaseService.eventsUserList;
                }
            }
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        System.out.println(position);
        List<EventModel> events = new ArrayList<>();
        switch (position){
            case 0: //NO FILTER
                return;
            case 1: //Fall Detection
                events = filterEvents("fallDetection");
                System.out.println(events);
                break;
            case 2:
                events = filterEvents("earthquakeDetection");
                System.out.println(events);
                break;
            case 3:
                events = filterEvents("earthquake");
                System.out.println(events);
                break;
            case 4: //SOS
                events = filterEvents("SOS");
                System.out.println(events);
                break;
            case 5: //AbortSOS
                events = filterEvents("AbortSOS");
                System.out.println(events);
                break;
            default:

        }

        count.setText(getResources().getString(R.string.statistics_count,Integer.toString(events.size())));
        setView(events);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

   private List<EventModel> filterEvents(String eventType){
       List<EventModel> filteredEvents = new ArrayList<>();

       for(EventModel event: events){
           if(event.type.equals(eventType)){
               filteredEvents.add(event);
           }

       }

       //Ταξινομηση με την ημερομηνια
       Collections.reverse(filteredEvents);
       return filteredEvents;

   }

   private void setView (List<EventModel> events) {
       listView.setAdapter(new ItemAdapter(this,events));

   }


}
