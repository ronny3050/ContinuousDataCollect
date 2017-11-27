package com.debayan.continuousdatacollect.Modules;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.debayan.continuousdatacollect.Utils.FileWriter;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by debayan on 10/17/17.
 */

public class Heart {
    private FileWriter fileWriter;
    private String filePath;

    public void initialize(final Context context, String fp, FileWriter fw, int LIGHT_CHANGE) {
        filePath = fp;
        fileWriter = fw;
        SensorManager mySensorManager = (SensorManager)context.getSystemService(SENSOR_SERVICE);
        mySensorManager.registerListener(HumiditySensorListener, mySensorManager.getDefaultSensor(Sensor.TYPE_HEART_BEAT), SensorManager.SENSOR_DELAY_NORMAL);
        mySensorManager.registerListener(HumiditySensorListener, mySensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private final SensorEventListener HumiditySensorListener = new SensorEventListener(){

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_HEART_BEAT) {
                float heart_bate = event.values[0];
                Log.i("Heart Beat: ", String.valueOf(heart_bate));
            }
            if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                float heart_rate = event.values[0];
                Log.i("Heart Rate: ", String.valueOf(heart_rate));
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public void setFilePath(String fp) {
        filePath = fp;
    }
}
