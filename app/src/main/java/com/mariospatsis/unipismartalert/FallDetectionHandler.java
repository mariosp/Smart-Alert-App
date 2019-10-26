package com.mariospatsis.unipismartalert;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

interface FallDetectionListener
{
    public void onStatusChanged(boolean newStatus);
}

public class FallDetectionHandler implements SensorEventListener {
    private String TAG = "FALL DETECTION HANDLER";
    private FallDetectionListener listener;
    public SensorManager mSensorManager;
    private Sensor mSensor;
    //private long mlPreviousTime;
    private boolean moIsMin = false;
    private boolean moIsMax = false;
    private Context mContext;
    private int i;
    public static Boolean status;

    public FallDetectionHandler(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        registerListener();
    }

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


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            double loX = sensorEvent.values[0];
            double loY = sensorEvent.values[1];
            double loZ = sensorEvent.values[2];

            double loAccelerationReader = Math.sqrt(Math.pow(loX, 2)
                    + Math.pow(loY, 2)
                    + Math.pow(loZ, 2));
            //mlPreviousTime = System.currentTimeMillis();
            //Log.i(TAG, "loX : " + loX + " loY : " + loY + " loZ : " + loZ);
            //System.out.println(loAccelerationReader);
            if (loAccelerationReader <= 6.0) {
                moIsMin = true;
//                Log.i(TAG, "min");
            }

            if (moIsMin) {
                i++;
                //Log.i(TAG, " loAcceleration : " + loAccelerationReader);
                if (loAccelerationReader >= 25) {
//                    long llCurrentTime = System.currentTimeMillis();
//                    System.out.println(llCurrentTime);
//                    long llTimeDiff = llCurrentTime - mlPreviousTime;
//                    System.out.println(mlPreviousTime);
//                    System.out.println(llTimeDiff);
//                    Log.i(TAG, "loTime :" + llTimeDiff);
//                    if (llTimeDiff >= 10) {
                        moIsMax = true;
                        Log.i(TAG, "max");
//                    }
                }

            }

            if (moIsMin && moIsMax) {
                Toast.makeText(mContext, "FALL DETECTED!!!!!", Toast.LENGTH_LONG).show();
                i = 0;
                moIsMin = false;
                moIsMax = false;
                setFallDetection(true);
            }

            if (i > 10) {
                i = 0;
                moIsMin = false;
                moIsMax = false;
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void setFallDetection(boolean fallDetectionstatus){
        if(listener !=null){
            listener.onStatusChanged(fallDetectionstatus);

        }
    }

    public void setFallDetectionListener(FallDetectionListener listener){
        this.listener = listener;
    }
}
