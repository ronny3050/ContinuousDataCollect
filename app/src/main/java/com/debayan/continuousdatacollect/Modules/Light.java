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

public class Light {

    private FileWriter fileWriter;
    private String filePath;

    int change = 0;
    int prev = 0;
    long time = 0;
    int freq = 0;

    public void initialize(final Context context, String fp, FileWriter fw, int FREQ, int LIGHT_CHANGE) {
        fileWriter = fw;
        filePath = fp;
        change = LIGHT_CHANGE;
        freq = FREQ;
        SensorManager mySensorManager = (SensorManager)context.getSystemService(SENSOR_SERVICE);
        mySensorManager.registerListener(LightSensorListener, mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);

    }

    private final SensorEventListener LightSensorListener = new SensorEventListener(){

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_LIGHT){
                int light = (int) event.values[0];
                if (Math.abs(prev - light) > change) {
                    long now = System.currentTimeMillis() / (long)1000;

                    if( (now- time) > freq) {

                        prev = light;
                        JSONObject lightTrace = new JSONObject();
                        try {
                            lightTrace.put("Value", light);
                            lightTrace.put("Acc", event.accuracy);
                            lightTrace.put("Timestamp", System.currentTimeMillis() / 1000);
                            Log.i("CHANGE", "LIGHT " + lightTrace + " old " + time + " now " + now + " freq " + freq);
                            time =  System.currentTimeMillis() / (long)1000;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        fileWriter.addData(filePath, FileWriter.DATA_TYPE.LIGHT,
                                new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                                , lightTrace);
                    }
                }

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
