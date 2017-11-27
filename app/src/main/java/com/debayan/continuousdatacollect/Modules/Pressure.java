package com.debayan.continuousdatacollect.Modules;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.debayan.continuousdatacollect.Utils.FileWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by debayan on 10/17/17.
 */

public class Pressure {
    private FileWriter fileWriter;
    private String filePath;

    int prev = 0;
    int change =0 ;

    public void initialize(final Context context, String fp, FileWriter fw, int PRESSURE_CHANGE) {
        filePath = fp;
        fileWriter = fw;
        change = PRESSURE_CHANGE;
        SensorManager mySensorManager = (SensorManager)context.getSystemService(SENSOR_SERVICE);
        mySensorManager.registerListener(PressureSensorListener, mySensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private final SensorEventListener PressureSensorListener = new SensorEventListener(){

        @Override
        public void onSensorChanged(SensorEvent event) {
            int atmoshperic_pressure = (int) event.values[0];
            if (Math.abs(prev - atmoshperic_pressure) > change){
                prev = atmoshperic_pressure;
                JSONObject pressureTrace = new JSONObject();
                try {
                    pressureTrace.put("Value", atmoshperic_pressure);
                    pressureTrace.put("Acc", event.accuracy);
                    pressureTrace.put("Timestamp", System.currentTimeMillis()/1000);
                    Log.i("CHANGE", "PRESSURE " + pressureTrace);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                fileWriter.addData(filePath, FileWriter.DATA_TYPE.PRESSURE,
                        new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                        , pressureTrace);
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
