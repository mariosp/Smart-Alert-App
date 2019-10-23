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

interface SeismicDetectionListener
{
    public void onStatusChanged(boolean newStatus);
}

public class SeismicDetectionHandler implements SensorEventListener {
    private String TAG = "SEISMIC DETECTION HANDLER";
    private SeismicDetectionListener listener;
    public SensorManager mSensorManager;
    private Sensor mSensor;
    private boolean moIsMin = false;
    private boolean moIsMax = false;
    private Context mContext;
    private int i;

    public SeismicDetectionHandler(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        registerListener();
    }

    public void registerListener(){
        mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterListener(){
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            double loX = sensorEvent.values[0];
            double loY = sensorEvent.values[1];
            double loZ = sensorEvent.values[2];

            double loAccelerationReader = Math.sqrt(Math.pow(loX, 2)
                    + Math.pow(loY, 2)
                    + Math.pow(loZ, 2));
            System.out.println(loAccelerationReader);
            if (loAccelerationReader >= 11) {
                Toast.makeText(mContext, "EARTHQUAKE DETECTED!!!!!", Toast.LENGTH_LONG).show();
                setSeismicDetection(true);
            }


        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void setSeismicDetection(boolean seismicDetectionstatus){
        if(listener !=null){
            listener.onStatusChanged(seismicDetectionstatus);

        }
    }

    public void setSeismicDetectionListener(SeismicDetectionListener listener ){
        this.listener = listener;
    }

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