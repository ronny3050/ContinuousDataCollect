package com.debayan.continuousdatacollect.Modules;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.debayan.continuousdatacollect.Utils.FileWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by debayan on 10/17/17.
 */

public class Screen {
    private FileWriter fileWriter;
    private String filePath;


    public void initialize(final Context ctx, String fp, FileWriter fw) {
        fileWriter = fw;
        filePath = fp;
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        ctx.registerReceiver(mReceiver, filter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                JSONObject screenTrace = new JSONObject();
                try {
                    screenTrace.put("Value", "off");
                    screenTrace.put("Timestamp", System.currentTimeMillis() / 1000);
                    Log.i("CHANGE", "SCREEN " + "off");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                fileWriter.addData(filePath, FileWriter.DATA_TYPE.SCREEN,
                        new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                        , screenTrace);
            }
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                JSONObject screenTrace = new JSONObject();
                try {
                    screenTrace.put("Value", "on");
                    screenTrace.put("Timestamp", System.currentTimeMillis() / 1000);
                    Log.i("CHANGE", "SCREEN " + "on");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                fileWriter.addData(filePath, FileWriter.DATA_TYPE.SCREEN,
                        new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                        , screenTrace);
                // Face
//                faceIntent = new Intent(context, Face.class);
//                context.startService(faceIntent);
//                context.bindService(faceIntent, mConnection, Context.BIND_AUTO_CREATE);
            }
        }
    };

    public void setFilePath(String fp) {
        filePath = fp;
    }

}
