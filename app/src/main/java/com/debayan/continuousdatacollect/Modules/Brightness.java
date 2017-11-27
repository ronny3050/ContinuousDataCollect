package com.debayan.continuousdatacollect.Modules;

import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.debayan.continuousdatacollect.Utils.FileWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by debayan on 10/17/17.
 */

public class Brightness {
    private FileWriter fileWriter;
    private String filePath;

    int prev = 0;


    public void initialize(final Context context, String fp, FileWriter fw, int BRIGHTNESS_SAMPLING_RATE) {
        fileWriter = fw;
        filePath = fp;



        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        try {

                            int brightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                            if(prev != brightness) {
                                prev = brightness;
                                JSONObject brightnessTrace = new JSONObject();
                                try {
                                    brightnessTrace.put("Value", brightness);
                                    brightnessTrace.put("Timestamp", System.currentTimeMillis() / 1000);
                                    Log.i("CHANGE", "BRIGHTNESS " + brightnessTrace);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                fileWriter.addData(filePath, FileWriter.DATA_TYPE.BRIGHTNESS,
                                        new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                                        , brightnessTrace);
                            }
                        }
                        catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, BRIGHTNESS_SAMPLING_RATE);

    }

    public void setFilePath(String fp) {
        filePath = fp;
    }
}
