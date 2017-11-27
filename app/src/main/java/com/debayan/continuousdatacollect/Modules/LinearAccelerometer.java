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

public class LinearAccelerometer {
    private FileWriter fileWriter;
    private String filePath;

    float change = 0f;
    private static Float[] previousValues = {0f, 0f, 0f};
    long previousTimestamp = 0;
    int freq = 0;

    public void initialize(final Context context, String fp, FileWriter fw, int FREQUENCY, float CHANGE_IN_LINACCELEROMETER_MAGNITUDE) {
        fileWriter = fw;
        filePath = fp;
        change = CHANGE_IN_LINACCELEROMETER_MAGNITUDE;
        freq = FREQUENCY;
        SensorManager sensorManager=(SensorManager) context.getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(mSensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private SensorEventListener mSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION){
                if(System.currentTimeMillis() - previousTimestamp > freq / 10000) {
                    float ax = event.values[0];
                    float ay = event.values[1];
                    float az = event.values[2];
                    if (Math.abs(previousValues[0] - ax) > change ||
                            Math.abs(previousValues[1] - ay) > change ||
                            Math.abs(previousValues[2] - az) > change) {
                        previousValues = new Float[]{event.values[0], event.values[1], event.values[2]};
                        previousTimestamp = System.currentTimeMillis();
                        JSONObject accelerometerTrace = new JSONObject();
                        try {
                            accelerometerTrace.put("X", ax);
                            accelerometerTrace.put("Y", ay);
                            accelerometerTrace.put("Z", az);
                            accelerometerTrace.put("Acc", event.accuracy);
                            accelerometerTrace.put("Timestamp", System.currentTimeMillis()/1000);
                            Log.i("CHANGE", "LIN-ACCELEROMETER " + accelerometerTrace);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        fileWriter.addData(filePath, FileWriter.DATA_TYPE.LINEAR_ACCELERATION,
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
