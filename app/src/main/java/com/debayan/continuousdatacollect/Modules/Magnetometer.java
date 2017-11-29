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
import static android.hardware.Sensor.TYPE_MAGNETIC_FIELD;

/**
 * Created by debayan on 10/17/17.
 */

public class Magnetometer {
    private FileWriter fileWriter;
    private String filePath;
    float change = 0;

    long previousTimestamp = 0;
    int freq;

    float previousX = 0;
    float previousY = 0;
    float previousZ = 0;

    public void initialize(final Context context, String fp, FileWriter fw, int FREQ, float CHANGE_IN_MAGENTOMETER_MAGNITUDE) {
        fileWriter = fw;
        filePath = fp;
        change = CHANGE_IN_MAGENTOMETER_MAGNITUDE;
        freq = FREQ;

        SensorManager mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorEventListener, mSensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private SensorEventListener mSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                float ax = event.values[0];
                float ay = event.values[1];
                float az = event.values[2];
                if ( (System.currentTimeMillis() / 1000 - previousTimestamp) > freq) {
                    if (Math.abs(previousX - ax) > change ||
                            Math.abs(previousY - ay) > change ||
                            Math.abs(previousZ - az) > change) {
                        previousX = ax;
                        previousY = ay;
                        previousZ = az;
                        previousTimestamp = System.currentTimeMillis()/1000;
                        JSONObject magnetometerTrace = new JSONObject();
                        try {
                            magnetometerTrace.put("X", ax);
                            magnetometerTrace.put("Y", ay);
                            magnetometerTrace.put("Z", az);
                            magnetometerTrace.put("Acc", event.accuracy);
                            magnetometerTrace.put("Timestamp", System.currentTimeMillis() / 1000);
                            Log.i("CHANGE", "MAGNETOMETER " + magnetometerTrace);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        fileWriter.addData(filePath, FileWriter.DATA_TYPE.MAGNETOMETER,
                                new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                                , magnetometerTrace);
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