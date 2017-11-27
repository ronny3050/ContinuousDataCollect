package com.debayan.continuousdatacollect.Modules;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
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

public class Battery {
    private FileWriter fileWriter;
    private String filePath;
    private int prev = -1;

    public void initialize(final Context context, String fp, FileWriter fw) {
        fileWriter = fw;
        filePath = fp;
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(mReceiver, ifilter);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPct = (int) ((level * 100) / (float)scale);
            if (prev != batteryPct)
            {
                prev =  batteryPct;
                JSONObject batteryTrace = new JSONObject();

                try {
                    batteryTrace.put("Status", intent.getExtras().getInt(BatteryManager.EXTRA_STATUS));
                    batteryTrace.put("Perc", batteryPct);
                    batteryTrace.put("Voltage", intent.getExtras().getInt(BatteryManager.EXTRA_VOLTAGE));
                    batteryTrace.put("Temp", intent.getExtras().getInt(BatteryManager.EXTRA_TEMPERATURE) / 10);
                    batteryTrace.put("Adapter", intent.getExtras().getInt(BatteryManager.EXTRA_PLUGGED));
                    batteryTrace.put("Health",  intent.getExtras().getInt(BatteryManager.EXTRA_HEALTH));
                    batteryTrace.put("Timestamp", System.currentTimeMillis()/1000);
                    Log.i("CHANGE", "BATTERY " + batteryPct);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                fileWriter.addData(filePath, FileWriter.DATA_TYPE.BATTERY,
                        new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                        , batteryTrace);

            }
        }
    };
    public void setFilePath(String fp) {
        filePath = fp;
    }
}
