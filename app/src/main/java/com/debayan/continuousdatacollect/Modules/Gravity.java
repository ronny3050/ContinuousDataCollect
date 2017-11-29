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

public class Gravity {

    float change = 0;
    long previousTimestamp = 0;
    int freq;
    private static Float[] previousValues = {0f, 0f, 0f};

    private String TAG = "GRAVITY";

    private FileWriter fileWriter;
    private String filePath;
    private Context ctx;

    public void initialize(final Context context, String fp, FileWriter fw, int FREQ, float CHANGE_IN_MAGNETOMETER) {
        filePath = fp;
        fileWriter = fw;
        change = CHANGE_IN_MAGNETOMETER;
        freq = FREQ;

        SensorManager sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        Sensor mSensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        /* Initialize the gravity sensor */
        if (mSensorGravity != null) {
            Log.i(TAG, "Gravity sensor available. (TYPE_GRAVITY)");
            sensorManager.registerListener(mSensorEventListener,
                    mSensorGravity, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.i(TAG, "Gravity sensor unavailable. (TYPE_GRAVITY)");
        }

    }

    private SensorEventListener mSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if ( (System.currentTimeMillis() / 1000 - previousTimestamp) > freq) {
                if(     Math.abs(previousValues[0] - event.values[0]) > change ||
                        Math.abs(previousValues[1] - event.values[1]) > change ||
                        Math.abs(previousValues[2] - event.values[2]) > change ) {
                    previousValues = new Float[]{event.values[0], event.values[1], event.values[2]};
                    previousTimestamp = System.currentTimeMillis() / 1000;
                    JSONObject gravityTrace = new JSONObject();
                    try {
                        gravityTrace.put("X", event.values[0]);
                        gravityTrace.put("Y", event.values[1]);
                        gravityTrace.put("Z", event.values[2]);
                        gravityTrace.put("Acc", event.accuracy);
                        gravityTrace.put("Timestamp", System.currentTimeMillis()/1000);
                        Log.i("CHANGE", "GRAVITY " + gravityTrace);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    fileWriter.addData(filePath, FileWriter.DATA_TYPE.GRAVITY,
                            new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                            , gravityTrace);
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
