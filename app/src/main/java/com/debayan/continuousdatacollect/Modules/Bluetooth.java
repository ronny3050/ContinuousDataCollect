package com.debayan.continuousdatacollect.Modules;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import com.debayan.continuousdatacollect.Utils.FileWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED;

/**
 * Created by debayan on 10/16/17.
 */

public class Bluetooth {
    private FileWriter fileWriter;
    private String filePath;

    private static boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        }
        else if(!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }

    public void initialize(final Context context, String fp, FileWriter fw, int BLUETOOTH_SAMPLING_RATE) {

        fileWriter = fw;
        filePath = fp;
        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mReceiver, filter);
        // Register for broadcasts when a device is discovered.
        IntentFilter filter2 = new IntentFilter(ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(mReceiver2, filter2);
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
                            setBluetooth(true);
                            bluetoothAdapter.startDiscovery();
                        }
                        catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, BLUETOOTH_SAMPLING_RATE);
    }

    private final  BroadcastReceiver mReceiver2 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i("CHANGE", "FINISHED BLUETOOTH DISCOVERY!!");
            setBluetooth(false);
        }
    };

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                JSONObject bluetoothTrace = new JSONObject();

                try {
                    bluetoothTrace.put("Name", deviceName);
                    bluetoothTrace.put("MAC", deviceHardwareAddress);
                    bluetoothTrace.put("Timestamp", System.currentTimeMillis()/1000);

                    Log.i("CHANGE", "BLUETOOTH " + bluetoothTrace);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                fileWriter.addData(filePath, FileWriter.DATA_TYPE.BLUETOOTH,
                        new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                        , bluetoothTrace);
            }
        }
    };

    public void setFilePath(String fp) {
        filePath = fp;
    }

}
