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
    String type;
    Button sosBtn;
    Button abortBtn;
    public TextView mainTitle;
    CountDownTimer countDownTimer;
    boolean countDownTimerIsRunning = false;
    private FallDetectionHandler falldetection;

    private final static int REQUESTCODE = 325;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 326;
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
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationService = new LocationService();
        sosBtn = findViewById(R.id.btn_sos);
        abortBtn = findViewById(R.id.btn_abort);
        mainTitle = findViewById(R.id.main_title);
        sosBtn.setOnClickListener(this);
        abortBtn.setOnClickListener(this);
        //checkPermision();
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
                    //setupEarthquakeDetection();
                    if(falldetection!=null){
                        falldetection.unregisterListener();
                        cancelTimer();
                    }
                    mainTitle.setText(R.string.main_title2);
                    setupFallDetection();
                    //setupEarthquakeDetection();
                }else {
                    type = "fallDetection";
                    mainTitle.setText(R.string.main_title1);
                    setupFallDetection();
                    //setupFallDetection();

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
                //Notification notification = new Notification();
//                String test = LocationService.getCity();
//                System.out.println(test);
                handleEvent("SOS");
                break;
            case R.id.btn_abort:
                if(type == "fallDetection" && countDownTimerIsRunning) {
                    cancelTimer();
                    Toast.makeText(this, "Aborted", Toast.LENGTH_LONG).show();
                    mainTitle.setText(R.string.main_title1);
                }
                break;
        }
    }

    public void checkPermision(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUESTCODE);
        }
        else{
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,
                    locationService);
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d("SMS PERIMISSION", "NOT GRANDED");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        } else {
            // Permission already granted.
        }


    }
    private void checkPermissions() {
        List<String> PERMISSIONS = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                PERMISSIONS.add(Manifest.permission.ACCESS_FINE_LOCATION);
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
//        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
//            Toast.makeText(this,"Yesss I have GPS",Toast.LENGTH_SHORT).show();
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            System.out.println("test");
//            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
//                    locationService);
//        } else
//            Toast.makeText(this,"I need this permission!...",Toast.LENGTH_SHORT).show();

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

    private void setupEarthquakeDetection() {
    }

    private void handleEvent(String type){
        String eventType = type;
        String lat = Double.toString(LocationService.latitude);
        String lon = Double.toString(LocationService.longitude);
        long timestamp = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        String date = DateFormat.format("dd-MM-yyyy HH:mm", cal).toString();
        System.out.println(date);

        if(type != "earthquakeDetection") { //Στελνουμε μηνυμα σε καθε περιπτωση εκτος απο την περιπτωση της ανιχνευσης σεισμου
            Notification notification = new Notification();
            notification.sendNotification(type, lat, lon, date);
        }
    }
}
