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

/* Notification κλαση
    Ειδοποιει αλλους χρηστες με την αποστολη sms
*  */
public class Notification {
    static Activity mainActivity;
    String[] contacts;
    SmsManager smsManager;
    TextToSpeech tts;

    public Notification(Activity activity) {
        mainActivity = activity;
        getContacts();
        smsManager = SmsManager.getDefault();
    }

    /* getMessage
     *   Αποστολη SMS
     *  */
    public void sendNotification(String notificationType,String lat,String lon, String time){
        String msg = getMessage(notificationType,lat,lon,time);

        for(String contact:contacts) {
            System.out.println("SENT");
            smsManager.sendTextMessage(contact, null, msg, null, null);
        }
        Toast.makeText(mainActivity, msg, Toast.LENGTH_LONG).show();
    }

    /* getMessage
    *   Επιστροφη καταλληλου μηνυματος με βασει τον τυπο του event
    *  */
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


    /* getContacts
        Επιστροφη των αποθηκευμενων επαφων απο την sharedPreference
    * */
    private void getContacts(){
        SharedPreferences sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);
        Set<String> contacts = sharedPref.getStringSet("ContactNumbers",null);
        this.contacts = contacts.toArray(new String[contacts.size()]);

    }

    /*
    * setContacts
    * Αποθηκευση αριθμων στην sharedPreference
    * */
//    static void setContacts(Activity activity){
//        mainActivity = activity;
//        Set<String> contacts = new HashSet<String>();
//
//        //Add the numbers to send the text message
//        contacts.add("");
//
//        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putStringSet("ContactNumbers", contacts);
//        editor.apply();
//    }

    /* textToSpeech
    * Εκφωνιση περιεχομενου μηνυματος
    *  */
    private void textToSpeech(){
        final Handler handler = new Handler();
        tts = new TextToSpeech(mainActivity.getApplicationContext(),new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                System.out.println("text");
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                    for(int i=1; i<=3; i++){
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
