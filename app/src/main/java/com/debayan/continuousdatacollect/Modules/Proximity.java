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

public class Proximity {
    private FileWriter fileWriter;
    private String filePath;

    int freq = 0;
    long previousTimestamp = 0;

    public void initialize(final Context context, String fp, FileWriter fw,  int FREQ) {
        fileWriter = fw;
        filePath = fp;
        freq = FREQ;
        SensorManager mySensorManager = (SensorManager)context.getSystemService(SENSOR_SERVICE);
        mySensorManager.registerListener(ProximitySensorListener, mySensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);

    }

    private final SensorEventListener ProximitySensorListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {

                if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                    if(System.currentTimeMillis() - previousTimestamp > freq / 1000) {
                    previousTimestamp = System.currentTimeMillis();
                    JSONObject proximityTrace = new JSONObject();
                    try {
                        proximityTrace.put("Value", event.values[0]);
                        proximityTrace.put("Acc", event.accuracy);
                        proximityTrace.put("Timestamp", System.currentTimeMillis() / 1000);
                        Log.i("CHANGE", "PROXIMITY " + proximityTrace);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    fileWriter.addData(filePath, FileWriter.DATA_TYPE.PROXIMITY,
                            new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                            , proximityTrace);

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
