package com.debayan.continuousdatacollect.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by debayan on 10/16/17.
 */

public class FileWriter {

    public enum DATA_TYPE {GPS, MAGNETOMETER, ACCELEROMETER, GYROSCOPE, BATTERY, WIFI, LIGHT, PROXIMITY, BRIGHTNESS, PRESSURE, VOLUME, SCREEN, STEP, LINEAR_ACCELERATION, GRAVITY, TEMPERATURE, HUMIDITY, ROTATION, CELL, APPLICATIONS, NOTIFICATIONS, BLUETOOTH};


    public boolean initialize(Context context) {

        String android_id = Settings.Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID);
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ContinuousAuthentication/";
        new File(baseDir).mkdirs();
        String fileName =  android_id + "-info" + ".profile";
        String filePath = baseDir + File.separator + fileName;
        if(!new File(filePath).exists()) {
            JSONObject deviceInformation = new JSONObject();
            try {
                SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), MODE_PRIVATE);
                String userName = sharedPreferences.getString("NAME", "");
                String email = sharedPreferences.getString("EMAIL", "");
                deviceInformation.put("UserName", userName);
                deviceInformation.put("Email", email);
                deviceInformation.put("AndroidID", android_id);
                deviceInformation.put("Device", android.os.Build.DEVICE);
                deviceInformation.put("Model", android.os.Build.MODEL);
                deviceInformation.put("Product", android.os.Build.PRODUCT);
                deviceInformation.put("API", android.os.Build.VERSION.SDK);
                deviceInformation.put("OS", System.getProperty("os.version"));
                SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
                List<Sensor> sensor = sensorManager.getSensorList(Sensor.TYPE_ALL);
                JSONArray sensorArr = new JSONArray();
                for(Sensor sens:
                        sensor) {
                    JSONObject sensorInformation = new JSONObject();
                    sensorInformation.put("Type", sens.getStringType());
                    sensorInformation.put("Name", sens.getName());
                    sensorInformation.put("Vendor", sens.getVendor());
                    sensorInformation.put("Version", sens.getVersion());
                    sensorInformation.put("MaxRange", sens.getMaximumRange());
                    sensorInformation.put("MinDelay", sens.getMinDelay());
                    sensorInformation.put("Power", sens.getPower());
                    sensorInformation.put("Resolution", sens.getResolution());
                    sensorArr.put(sensorInformation);
                }
                deviceInformation.put("Sensors", sensorArr);
                writeToFile(filePath, deviceInformation.toString());
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void initialializeDataFile(String fp) {
        if(!new File(fp).exists()) {
            JSONObject obj = new JSONObject();
            try {
                JSONObject datObj = new JSONObject();
                for (int i = 0; i < DATA_TYPE.values().length; i++) {
                    datObj.put(DATA_TYPE.values()[i].toString(), new JSONArray());
                }
                obj.put("Data", datObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            writeToFile(fp, obj.toString());
        }
    }

    public void addData(String fp, DATA_TYPE data_type, String date, JSONObject jsonObjectArg) {
            initialializeDataFile(fp);
            JSONObject jsonObject = getAllData(fp);

            try {
                    JSONObject data = jsonObject.getJSONObject("Data");
                    JSONArray DataArr = data.getJSONArray(data_type.toString());
                    DataArr.put(DataArr.length(), jsonObjectArg);
                    data.put(data_type.toString(), DataArr);
                    jsonObject.put("Data", data);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            writeToFile(fp, jsonObject.toString());
    }

    public JSONObject getAllData(String fp) {

        StringBuilder text = new StringBuilder();
        String line;
        try {
            BufferedReader br = new BufferedReader(new FileReader(fp));
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(text.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    private void writeToFile(String fp, String data) {
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new
                        FileOutputStream(fp, false));
                outputStreamWriter.write(data);
                outputStreamWriter.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
    }
}
