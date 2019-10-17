package com.mariospatsis.unipismartalert;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class FirebaseService {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference usersRef = database.getReference().child("users");
    private String FCMToken;
    private String KEY_USERS = "users";

    public FirebaseService() {
    }

    public void getFCMToken(){
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.mainActivity,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                FCMToken = instanceIdResult.getToken();
                System.out.println("FCM" + FCMToken);
            }
        });
    }

    public void insertEvent(EventModel event){
        usersRef.child(FCMToken).push().setValue(event);
    }

    public void getEvents(){
        //final CountDownLatch done = new CountDownLatch(1);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<EventModel> eventsList = deconstructData(dataSnapshot);
                //done.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

//        try {
//            done.await(); //Περιμενου μεχρι να γυρισει η απαντηση απο firebase
//        } catch(InterruptedException e) {
//            e.printStackTrace();
//        }
        //return message[0];
    }

    private List<EventModel> deconstructData(DataSnapshot dataSnapshot){
        List<EventModel> events = new ArrayList<>();
        for(DataSnapshot perUser: dataSnapshot.getChildren() ){
            for(DataSnapshot perEvent: perUser.getChildren() ){
                events.add(perEvent.getValue(EventModel.class));
            }
        }
        return events;
    }

}
