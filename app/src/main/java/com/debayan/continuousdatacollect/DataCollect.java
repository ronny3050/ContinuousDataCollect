package com.debayan.continuousdatacollect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by debayan on 10/16/17.
 * This is a BroadcastReceiver that starts data collection service on device reboot.
 * This is required in-case device shuts off.
 */

public class DataCollect extends BroadcastReceiver {


    @Override
    public void onReceive(final Context context, Intent arg1) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        Log.i("DataCollect", "Started");
    }



}
