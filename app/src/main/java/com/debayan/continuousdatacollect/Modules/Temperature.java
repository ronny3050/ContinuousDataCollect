package com.debayan.continuousdatacollect.Modules;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
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

public class Temperature {

    private final String TAG = "TEMPERATURE";

    private FileWriter fileWriter;
    private String filePath;

    long previousTimestamp = 0;
    int freq = 0;
    float prev = 0f;
    float change = 0f;

    public void initialize(final Context context, String fp, FileWriter fw, int LIGHT_FREQ, float LIGHT_CHANGE) {
        filePath = fp;
        fileWriter = fw;
        freq = LIGHT_FREQ;
        change = LIGHT_CHANGE;
        SensorManager mySensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        if (mySensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
            mySensorManager.registerListener(TempSensorListener, mySensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE), SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.i(TAG, "Temperature sensor unavailable. (TYPE_AMBIENT_TEMPERATURE)");
        }
    }

    private final SensorEventListener TempSensorListener = new SensorEventListener(){

        @Override
        public void onSensorChanged(SensorEvent event) {
            float ambient_temperature = event.values[0];
            if (System.currentTimeMillis() - previousTimestamp > freq / 1000) {
                if (Math.abs(ambient_temperature - prev) > change) {
                    previousTimestamp =  SystemClock.elapsedRealtime();
                    prev = ambient_temperature;
                    JSONObject lightTrace = new JSONObject();
                    try {
                        lightTrace.put("Value", ambient_temperature);
                        lightTrace.put("Acc", event.accuracy);
                        lightTrace.put("Timestamp", System.currentTimeMillis() / 1000);
                        Log.i("CHANGE", "TEMPERATURE " + lightTrace);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    fileWriter.addData(filePath, FileWriter.DATA_TYPE.TEMPERATURE,
                            new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                            , lightTrace);
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
