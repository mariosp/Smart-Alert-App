package com.mariospatsis.unipismartalert;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;


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

}
