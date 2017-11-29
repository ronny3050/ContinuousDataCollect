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
 * Created by debayan on 10/18/17.
 */

public class Rotation {
    private FileWriter fileWriter;
    private String filePath;

    float change = 0;
    private static Float[] previousValues = {0f, 0f, 0f};
    long previousTimestamp = 0;
    int freq = 0;

    public void initialize(final Context context, String fp, FileWriter fw, int FREQUENCY, float CHANGE_IN_ACCELEROMETER_MAGNITUDE) {
        fileWriter = fw;
        filePath = fp;
        change = CHANGE_IN_ACCELEROMETER_MAGNITUDE;
        freq = FREQUENCY;
        SensorManager sensorManager=(SensorManager) context.getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(mSensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private SensorEventListener mSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType()==Sensor.TYPE_ROTATION_VECTOR){
                if( (System.currentTimeMillis() / 1000 - previousTimestamp) > freq) {
                    float ax = event.values[0];
                    float ay = event.values[1];
                    float az = event.values[2];
                    if (Math.abs(previousValues[0] - ax) > change ||
                            Math.abs(previousValues[1] - ay) > change ||
                            Math.abs(previousValues[2] - az) > change) {
                        previousValues = new Float[]{event.values[0], event.values[1], event.values[2]};
                        previousTimestamp = System.currentTimeMillis() / 1000;
                        JSONObject accelerometerTrace = new JSONObject();
                        try {
                            accelerometerTrace.put("X", ax);
                            accelerometerTrace.put("Y", ay);
                            accelerometerTrace.put("Z", az);
                            if (event.values.length == 4) {
                                accelerometerTrace.put("Scalar", event.values[3]);
                            }
                            accelerometerTrace.put("Acc", event.accuracy);
                            accelerometerTrace.put("Timestamp", System.currentTimeMillis()/1000);
                            Log.i("CHANGE", "ROTATION " + accelerometerTrace);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        fileWriter.addData(filePath, FileWriter.DATA_TYPE.ROTATION,
                                new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                                , accelerometerTrace);
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
