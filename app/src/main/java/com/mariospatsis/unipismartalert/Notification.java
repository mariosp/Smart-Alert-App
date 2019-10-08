package com.mariospatsis.unipismartalert;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.SmsManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Notification {
    static Activity mainActivity;
    String[] contacts;
    SmsManager smsManager;

    public Notification() {
        getContacts();
        smsManager = SmsManager.getDefault();
    }

    public void sendNotification(String notificationType,String lat,String lon, String time){
        String msg = getMessage(notificationType,lat,lon,time);

        for(String contact:contacts) {
            System.out.println("SENT");
            smsManager.sendTextMessage(contact, null, msg, null, null);
        }

    }

    private String getMessage(String notificationType,String lat,String lon, String time){
        String type ="";
        if(notificationType == "SOS"){
            type = mainActivity.getResources().getString(R.string.typeSOS);
        }else if(notificationType == "fallDetection"){
            type = mainActivity.getResources().getString(R.string.typeFall);
        }else if(notificationType == "Abort"){

        }

        return type + ". " + mainActivity.getResources().getString(R.string.helpMsg,lat,lon);
    }


    private void getContacts(){
        SharedPreferences sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);
        Set<String> contacts = sharedPref.getStringSet("ContactNumbers",null);
        this.contacts = contacts.toArray(new String[contacts.size()]);

    }

    static void setContacts(Activity activity){
        mainActivity = activity;
        Set<String> contacts = new HashSet<String>();

        contacts.add("6980477426");
        contacts.add("6980477427");

        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet("ContactNumbers", contacts);
        editor.apply();
    }

}
