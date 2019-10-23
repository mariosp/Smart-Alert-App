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


interface FirebaseListener
{
    public void onStatusChanged(boolean newStatus);
}
public class FirebaseService {
   // private static final FirebaseService instance = new FirebaseService();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference usersRef = database.getReference().child("users");
    private String FCMToken;
    private String KEY_USERS = "users";

    private FirebaseListener listener;
    public List<EventModel> eventsList;

    public FirebaseService() {
    }

//    //public static FirebaseService getInstance(){
//        return instance;
//    }

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
        System.out.println(FCMToken);
        usersRef.child(FCMToken).push().setValue(event);
    }

    public void getEvents(){
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventsList = deconstructData(dataSnapshot);
                setFirebaseStatus(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
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

    public void setFirebaseStatus(boolean onCompleteStatus){
        if(listener !=null){
            listener.onStatusChanged(onCompleteStatus);

        }
    }

    public void setFirebaseListener(FirebaseListener listener){
        this.listener = listener;
    }

}
