package com.heartbeat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Copyright for the code: Henri Lavikainen
 * Made in Finland.  2020 - 2021
 *
 * Font is DSEG by Keshikan. Licensed under SIL Open Font License (OFL)
 */
public class MainActivity extends WearableActivity implements SensorEventListener {

    private TextView HeartBeat;
    private TextView HeartBeatMax;
    private TextView HeartBeatMin;
    private int maxValue=0;
    private int minValue=0;
    private static final String TAG = MainActivity.class.getSimpleName();

    //Used to track which screen we are on. It should be possible to use view id or something better.
    //TODO: Improve this see comment above.
    enum ScreenState {heartbeat,exit}

    private ScreenState currentState=ScreenState.heartbeat;

    public void onExitYes(View view)
    {
        finishAndRemoveTask();
    }
    public void onExitNo(View view)
    {
        currentState=ScreenState.heartbeat;
        setContentView(R.layout.activity_main);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        HeartBeat = findViewById(R.id.heartbeat);
        HeartBeatMax = findViewById(R.id.heartbeatmax);
        HeartBeatMin = findViewById(R.id.heartbeatmin);
        HeartBeat.setText("RESTART APP");
        HeartBeatMax.setVisibility(View.INVISIBLE);
        HeartBeatMin.setVisibility(View.INVISIBLE);
        permissionRequest(); //TODO: This should be handled better... Currently restart is needed even if user gives permissions.

        readSensor();

        // Enables Always-on
        setAmbientEnabled();
    }
    private void readSensor() {
        SensorManager mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        Sensor mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mSensorManager.registerListener( this, mHeartRateSensor, SensorManager.SENSOR_DELAY_UI);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            if((int)event.values[0]==0) {
                //Heartrate is 0. This means sensor is available but reading cannot be made.
                //Caused either sensor starting up or watch has been removed from writs.
                //Do not log minimum value and display text "NO PULSE"
                HeartBeat.setText("NO PULSE");
                return;
            }
            String msg = "" + (int)event.values[0];
            if(HeartBeatMax.getVisibility()==View.INVISIBLE && (int)event.values[0]!=0){
                HeartBeatMax.setVisibility(View.VISIBLE);
                HeartBeatMin.setVisibility(View.VISIBLE);
                maxValue=(int)event.values[0];
                minValue=(int)event.values[0];
            }
            if((int)event.values[0]>maxValue)maxValue=(int)event.values[0];
            if((int)event.values[0]<minValue)minValue=(int)event.values[0];
            HeartBeat.setText(msg + " bpm");
            HeartBeatMax.setText("MAX: " + maxValue + " bpm");
            HeartBeatMin.setText("MIN: " + minValue + " bpm");
            //Log.d(TAG, msg);
        }

    }

    //Handle button presses
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (event.getRepeatCount() == 0) {
            if (keyCode == KeyEvent.KEYCODE_STEM_1) {
                event.startTracking();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_STEM_2) {
                event.startTracking();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_STEM_3) {
                event.startTracking();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    //Handle long presses
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event){
        Log.d(TAG, String.valueOf(keyCode));
        if (keyCode == KeyEvent.KEYCODE_STEM_1) {
            if(currentState==ScreenState.heartbeat){
                currentState=ScreenState.exit;
                setContentView(R.layout.exit_layout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_STEM_2) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_STEM_3) {
            //Button 3 is held. Exiting!
            //finishAndRemoveTask();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void permissionRequest(){
        //TODO: Negative answer not handled. Positive answer requires restart.
        if (checkSelfPermission(Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.BODY_SENSORS}, 1);
        }
        else{
            //Log.d(TAG,"ALREADY GRANTED");
        }
    }

}
