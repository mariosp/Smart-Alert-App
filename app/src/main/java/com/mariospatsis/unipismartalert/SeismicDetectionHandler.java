package com.mariospatsis.unipismartalert;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.TimeUnit;

/*
 * Interface  SeismicDetectionListener
 * με μεθοδο onStatusChanged
 * Γινεται override η μεθοδος στην main Activity και χρησιμοποιειται σαν listener οταν υπαρχει εντοπισμος δονησης
 * */
interface SeismicDetectionListener
{
    public void onStatusChanged(boolean newStatus);
}

/*
 *  H Κλαση SeismicDetectionHandler διαχειριζεται τις καταστασεις του επιταχυνσιομετρου
 *  και επιστρεφει στην MainActivity status = true μεσω της μεθοδου onStatusChanged οταν
 *  υπαρχει εντοπισμος δονησης του κινητου (seismicDetection)
 *
 *   */
public class SeismicDetectionHandler implements SensorEventListener {
    private String TAG = "SEISMIC DETECTION HANDLER";
    private SeismicDetectionListener listener;
    public SensorManager mSensorManager;
    private Sensor mSensor;
    private boolean moIsMin = false;
    private boolean moIsMax = false;
    private Context mContext;
    private int i;
    public static Boolean status;

    public SeismicDetectionHandler(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // o sensor που θα χρησιμοποιηθει (TYPE_ACCELEROMETER)
        registerListener();
    }

    /* Εγγραφη του listener SensorEventListener (this) που κανει implement η κλαση  */

    public void registerListener(){
        status = true;
        mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterListener(){
        status = false;
        mSensorManager.unregisterListener(this);
    }

    public static Boolean getListenerStatus(){
        return status;
    }


    /*
     * Για καθε event απο το επιταχυνσιομετρο
     * */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            double loX = sensorEvent.values[0];
            double loY = sensorEvent.values[1];
            double loZ = sensorEvent.values[2];

            double loAccelerationReader = Math.sqrt(Math.pow(loX, 2)
                    + Math.pow(loY, 2)
                    + Math.pow(loZ, 2));
            if (loAccelerationReader >= 11) {
                Toast.makeText(mContext, "EARTHQUAKE DETECTED!!!!!", Toast.LENGTH_LONG).show();
                setSeismicDetection(true);
            }


        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /* setSeismicDetection
     * καλει την μεθοδο onStatusChanged που εχει υλοποιηθει στην MainActivity
     */
    public void setSeismicDetection(boolean seismicDetectionstatus){
        if(listener !=null){
            listener.onStatusChanged(seismicDetectionstatus);

        }
    }

    /* setSeismicDetectionListener
     * Αποθηκευση του SeismicDetectionListener instance  (MainActivity) στα properties του αντικειμενου
     * για να γινει χρηστη του απο την setSeismicDetection
     */
    public void setSeismicDetectionListener(SeismicDetectionListener listener ){
        this.listener = listener;
    }

    /* seismicStatus
    * Ελεγχει αμα τα events ειναι σε κοντινη χρονικη περιοδο ( 6 δευτερολεπτα ) και
    * αμα η αποσταση ειναι κοντινη (5χμλ)
    * Επιστρεφει true / false */
    public boolean seismicStatus(List<EventModel> events, long eventTimestamp, double latd,double lond){
        long cureventTs = TimeUnit.MILLISECONDS.toSeconds(eventTimestamp);
        long eventTs;
        long diff;
        int times = 0;
        for(EventModel event: events){
            eventTs = TimeUnit.MILLISECONDS.toSeconds(event.timestamp);
            diff = eventTs - cureventTs;
            if(diff>= -3 && diff<= 3 && LocationService.getDistance(latd,lond, event.lat,event.lon)){
                times++;
            }
        }

        if(times> 1){
            return true;
        }else{
            return false;
        }


    }


}