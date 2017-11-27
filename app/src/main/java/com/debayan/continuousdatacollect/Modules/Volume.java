package com.debayan.continuousdatacollect.Modules;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
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

public class Volume  {

    private FileWriter fileWriter;
    private String filePath;
    private Context context;

    int previousVolume = 0;

    public void initialize(final Context ctx, String fp, FileWriter fw) {
        fileWriter = fw;
        filePath = fp;
        AudioManager audio = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        context = ctx;
        context.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver );
        JSONObject volumeTrace = new JSONObject();
        try {
            volumeTrace.put("Value", previousVolume);
            volumeTrace.put("Timestamp", System.currentTimeMillis() / 1000);
            Log.i("CHANGE", "VOLUME " + volumeTrace);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        fileWriter.addData(filePath, FileWriter.DATA_TYPE.VOLUME,
                new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                , volumeTrace);
    }

    private ContentObserver mSettingsContentObserver = new ContentObserver(new Handler()) {
        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)/audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            if(previousVolume != currentVolume) {
                previousVolume = currentVolume;
                JSONObject volumeTrace = new JSONObject();
                try {
                    volumeTrace.put("Value: ", currentVolume);
                    volumeTrace.put("Timestamp", System.currentTimeMillis() / 1000);
                    Log.i("CHANGE", "VOLUME " + volumeTrace);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                fileWriter.addData(filePath, FileWriter.DATA_TYPE.VOLUME,
                        new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                        , volumeTrace);
            }
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
        }
    };

    public void setFilePath(String fp) {
        filePath = fp;
    }
}
