package com.debayan.continuousdatacollect.Modules;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.debayan.continuousdatacollect.Utils.FileWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by debayan on 10/17/17.
 */

public class WiFi {
    private FileWriter fileWriter;
    private String filePath;
    WifiManager wifiManager;


    public void initialize(final Context context, String fp, FileWriter fw, int WIFI_SAMPLING_RATE) {
        fileWriter = fw;
        filePath = fp;

        wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(mWifiScanReceiver, filter);
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        try {
                            Log.i("CHANGE", "STARTED BLUETOOTH DISCOVERY!!");
                            wifiManager.startScan();
                        }
                        catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, WIFI_SAMPLING_RATE);
    }

    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> mScanResults = wifiManager.getScanResults();
                // add your logic here
                for (ScanResult sc:
                     mScanResults) {
                    String ssid = sc.SSID;
                    JSONObject wifiTrace = new JSONObject();
                    try {
                        wifiTrace.put("SSID", ssid);
                        wifiTrace.put("BSSID", sc.BSSID);
                        wifiTrace.put("Freq", sc.frequency);
                        wifiTrace.put("Level", sc.level);
                        wifiTrace.put("Timestamp", System.currentTimeMillis()/1000);
                        Log.i("CHANGE", "WIFI " + wifiTrace);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    fileWriter.addData(filePath, FileWriter.DATA_TYPE.WIFI,
                            new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                            , wifiTrace);
                }
            }
        }
    };

    public void setFilePath(String fp) {
        filePath = fp;
    }
}
