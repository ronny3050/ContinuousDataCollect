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
import static android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY;

/**
 * Created by debayan on 10/17/17.
 */

public class Humidity {
    private final String TAG = "HUMIDITY";
    private FileWriter fileWriter;
    private String filePath;

    long previousTimestamp = 0;
    int freq = 0;
    float prev = 0f;
    float change = 0f;

    public void initialize(final Context context, String fp, FileWriter fw, int LIGHT_FREQ,  int LIGHT_CHANGE) {
        filePath = fp;
        fileWriter = fw;
        freq = LIGHT_FREQ;
        change = LIGHT_CHANGE;
        SensorManager mySensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        if (mySensorManager.getDefaultSensor(TYPE_RELATIVE_HUMIDITY) != null)
            mySensorManager.registerListener(HumiditySensorListener, mySensorManager.getDefaultSensor(TYPE_RELATIVE_HUMIDITY), SensorManager.SENSOR_DELAY_NORMAL);
        else
            Log.i(TAG, "Humidity sensor unavailable. (TYPE_RELATIVE_HUMIDITY)");
    }

    private final SensorEventListener HumiditySensorListener = new SensorEventListener(){

        @Override
        public void onSensorChanged(SensorEvent event) {
            float humidity = event.values[0];
            if (System.currentTimeMillis() - previousTimestamp > freq / 1000) {
                if (Math.abs(humidity - prev) > change) {
                    previousTimestamp =  SystemClock.elapsedRealtime();
                    prev = humidity;
                    JSONObject lightTrace = new JSONObject();
                    try {
                        lightTrace.put("Value", humidity);
                        lightTrace.put("Acc", event.accuracy);
                        lightTrace.put("Timestamp", System.currentTimeMillis() / 1000);
                        Log.i("CHANGE", "TEMPERATURE " + lightTrace);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    fileWriter.addData(filePath, FileWriter.DATA_TYPE.HUMIDITY,
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
