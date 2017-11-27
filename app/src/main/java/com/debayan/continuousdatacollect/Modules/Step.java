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

public class Step {

    private String TAG = "MAGNETOMETER";

    FileWriter fileWriter;
    String filePath;

    public void initialize(final Context context, String fp, FileWriter fw) {
        fileWriter = fw;
        filePath = fp;
        SensorManager mySensorManager = (SensorManager)context.getSystemService(SENSOR_SERVICE);
        if(mySensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            mySensorManager.registerListener(LightSensorListener, mySensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        }else {
            Log.i(TAG, "Step sensor available. (TYPE_STEP_DETECTOR , TYPE_STEP_COUNTER)");
        }
    }

    private final SensorEventListener LightSensorListener = new SensorEventListener(){

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {

                JSONObject stepTrace = new JSONObject();
                try {
                    stepTrace.put("Timestamp", System.currentTimeMillis() / 1000);
                    Log.i("CHANGE", "STEP " + stepTrace);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                fileWriter.addData(filePath, FileWriter.DATA_TYPE.STEP,
                        new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                        , stepTrace);
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
