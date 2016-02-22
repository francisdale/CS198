package com.example.dale.cs198;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by DALE on 2/18/2016.
 */
public class CameraAccelerometer implements SensorEventListener{

    //Sensor accelerometer;
    //SensorManager sensorManager;

    public CameraAccelerometer(Sensor accelerometer,SensorManager sensorManager) {

        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);





    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
