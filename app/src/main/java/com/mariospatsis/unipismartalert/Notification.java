package com.mariospatsis.unipismartalert;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import android.os.Handler;

public class Notification {
    static Activity mainActivity;
    String[] contacts;
    SmsManager smsManager;
    TextToSpeech tts;

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
        Toast.makeText(mainActivity, msg, Toast.LENGTH_LONG).show();
    }

    private String getMessage(String notificationType,String lat,String lon, String time){
        String type ="";
        if(notificationType == "SOS"){
            textToSpeech();
            type = mainActivity.getResources().getString(R.string.typeSOS);
        }else if(notificationType == "fallDetection"){
            type = mainActivity.getResources().getString(R.string.typeFall);
        }else if(notificationType == "AbortSOS"){
            type = mainActivity.getResources().getString(R.string.typeAbort);
            return type + mainActivity.getResources().getString(R.string.abortMsg,lat,lon,time);
        }

        return type + ". " + mainActivity.getResources().getString(R.string.helpMsg,lat,lon,time);
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

    private void textToSpeech(){
        final Handler handler = new Handler();
        tts = new TextToSpeech(mainActivity.getApplicationContext(),new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                System.out.println("text");
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                    for(int i=1; i<=3; i++){
//                        tts.speak("HELP ME", TextToSpeech.QUEUE_ADD, null);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tts.speak("HELP ME", TextToSpeech.QUEUE_ADD, null);
                            }
                        }, 2000 * i);
                    }
                }
            }
        });
    }

}
