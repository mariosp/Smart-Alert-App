package com.mariospatsis.unipismartalert;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    static Activity mainActivity;
    UsbService mUsbService = new UsbService();
    FirebaseService mFirebaseService = new FirebaseService();
    String type;
    Button sosBtn;
    Button abortBtn;
    public TextView mainTitle;
    public TextView sosTitle;
    CountDownTimer countDownTimer;
    CountDownTimer countDownSOS;
    boolean countDownTimerIsRunning = false;
    boolean sosStatus = false;
    private FallDetectionHandler falldetection;
    private SeismicDetectionHandler seismicdetection;

    private final static int REQUESTCODE = 325;
    LocationManager mLocationManager;
    Uri notification;
    Ringtone r;
    private LocationListener locationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Notification.setContacts(this);
        mainActivity = this;
        mFirebaseService.getFCMToken();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationService = new LocationService();
        sosBtn = findViewById(R.id.btn_sos);
        abortBtn = findViewById(R.id.btn_abort);
        mainTitle = findViewById(R.id.main_title);
        sosTitle = findViewById(R.id.sos_text);
        sosBtn.setOnClickListener(this);
        abortBtn.setOnClickListener(this);
        checkPermissions();
        try {
            notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Κανουμε register to UsbService broadcast ωστε
        // να λαμβανουμε τα events οταν υπαρχει αλλαγη στην κατασταση του USB
        this.registerReceiver(mUsbService,new IntentFilter("android.hardware.usb.action.USB_STATE"));
        mUsbService.setOnUsbServiceStatusListener(new OnUsbServiseStatusListener() {
            @Override
            public void onStatusChanged(boolean newStatus) {
                if(newStatus){
                    type = "earthquakeDetection";
                    if(falldetection!=null){
                        falldetection.unregisterListener();
                        cancelTimer();
                    }
                    mainTitle.setText(R.string.main_title2);
                    //setupFallDetection();
                    setupEarthquakeDetection();
                }else {
                    type = "fallDetection";
                    mainTitle.setText(R.string.main_title1);
                    setupFallDetection();
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() { // Κανουμε unregister τον broadcaster οταν φευγουμε απο το activity
        super.onDestroy();
        this.unregisterReceiver(mUsbService);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_sos:
                sosStatus = true;
                sosTitle.setText(R.string.sos_title);
                countDownSOS =  new CountDownTimer(300000, 1000) { //timer στα 5 λεπτα ωστε να μπορει ο// χρηστης να στειλει μηνυμα ακυρωσης μεσα σε αυτα τα λεπτα
                    public void onTick(long millisUntilFinished) {
                    }
                    public void onFinish() {
                        sosTitle.setText("");
                        sosStatus = false;
                    }

                }.start();
                handleEvent("SOS");
                break;
            case R.id.btn_abort:
                if(type == "fallDetection" && countDownTimerIsRunning) {
                    cancelTimer();
                    Toast.makeText(this, "Aborted", Toast.LENGTH_LONG).show();
                    mainTitle.setText(R.string.main_title1);
                    falldetection.registerListener();
                } else if(sosStatus){
                    cancelSOSTimer();
                    handleEvent("AbortSOS");
                }
                break;
        }
    }

    private void checkPermissions() {
        List<String> PERMISSIONS = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                PERMISSIONS.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }else{
            System.out.println("GPS ENABLED");
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                    locationService);
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            PERMISSIONS.add(Manifest.permission.SEND_SMS);
        }

       if(!PERMISSIONS.isEmpty()){
           String[] array = PERMISSIONS.toArray(new String[PERMISSIONS.size()]);
           ActivityCompat.requestPermissions(this,
                   array,
                   REQUESTCODE);
       }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED) {
                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION))
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                            locationService);

            }

        }
    }

    private void setupFallDetection() {
        falldetection = new FallDetectionHandler(this);
        falldetection.setFallDetectionListener(new FallDetectionListener() {
            @Override
            public void onStatusChanged(boolean fallDetectionStatus) {
                if(fallDetectionStatus) {
                    falldetection.unregisterListener();
                    countDownTimerIsRunning = true;
                    countDownTimer =  new CountDownTimer(30000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            r.play();
                            mainTitle.setText(Long.toString(millisUntilFinished / 1000));
                        }

                        public void onFinish() {
                            countDownTimerIsRunning = false;
                            r.stop();
                            mainTitle.setText(R.string.main_title1);
                            falldetection.registerListener();
                            handleEvent("fallDetection");
                        }

                    }.start();
                }
            }
        });
    }

    private void cancelTimer(){
        countDownTimer.cancel();
        r.stop();
    }

    private void cancelSOSTimer(){
        countDownSOS.onFinish();
        countDownSOS.cancel();
    }

    private void setupEarthquakeDetection() {
        seismicdetection = new SeismicDetectionHandler(this);
        seismicdetection.setSeismicDetectionListener(new SeismicDetectionListener() {
            @Override
            public void onStatusChanged(boolean seismicDetectionStatus) {
                if(seismicDetectionStatus) {
                    seismicdetection.unregisterListener(); // Κανουμε unregistrer τον listener μεχρι να γινει η καταγραφη στην βαση και να δουμε αν ειναι οντως σεισμος
                    handleEvent("earthquakeDetection"); //καταγραφουμε στην βαση με type earthquakeDetection ωστε να κανουμε αναζητηση και σε αλλους χρηστες με το ιδιο type
                }
            }
        });
    }

    private void handleEvent(String type){
        String eventType = type;
        double latd = LocationService.latitude;
        double lond = LocationService.latitude;
        //String city = LocationService.getCity();
        String lat = Double.toString(latd);
        String lon = Double.toString(lond);
        long timestamp = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        String date = DateFormat.format("dd-MM-yyyy HH:mm", cal).toString();

        mFirebaseService.insertEvent(new EventModel(type, latd,lond,timestamp));
        if(type != "earthquakeDetection") { //Στελνουμε μηνυμα σε καθε περιπτωση εκτος απο την περιπτωση της ανιχνευσης σεισμου
            Notification notification = new Notification();
            notification.sendNotification(type, lat, lon, date);
        }

        if(type == "earthquakeDetection"){
            System.out.println("ERTHDETECTION EVENT TO FIRE");
            mFirebaseService.getEvents();
        }
    }
}
