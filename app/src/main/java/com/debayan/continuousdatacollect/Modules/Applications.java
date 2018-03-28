package com.debayan.continuousdatacollect.Modules;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.debayan.continuousdatacollect.Utils.FileWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by debayan on 10/18/17.
 */


public class Applications extends AccessibilityService {

    long prev = 0;
    int freq = 20;
    String filePath;

    public void setFilePath(String fp) {
        filePath = fp;
    }

    /**
     * Given a package name, get application label in the default language of the device
     *
     * @param package_name
     * @return appName
     */
    private String getApplicationName(String package_name) {
        PackageManager packageManager = getPackageManager();
        ApplicationInfo appInfo;
        try {
            appInfo = packageManager.getApplicationInfo(package_name, PackageManager.GET_META_DATA);
        } catch (final PackageManager.NameNotFoundException e) {
            appInfo = null;
        }
        String appName = "";
        if (appInfo != null && packageManager.getApplicationLabel(appInfo) != null) {
            appName = (String) packageManager.getApplicationLabel(appInfo);
        }
        return appName;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (System.currentTimeMillis() - prev > freq) {

            // Notification
            if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                Notification notificationDetails = (Notification) event.getParcelableData();
                if (notificationDetails != null) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("PackageName", event.getPackageName().toString());
                        jsonObject.put("AppName", getApplicationName(event.getPackageName().toString()));
                        int sound = 0;
                        int vibrate = 0;
                        if (notificationDetails.sound != null) {
                            sound = 1;
                            vibrate = 1;
                        }
                        jsonObject.put("Sound", sound);
                        jsonObject.put("Vibrate", vibrate);
                        jsonObject.put("Timestamp", System.currentTimeMillis() / 1000);
                        Log.i("CHANGE", "NOTIFICATION " + jsonObject);
                        prev = System.currentTimeMillis();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    new FileWriter().addData(filePath, FileWriter.DATA_TYPE.NOTIFICATIONS,
                            new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                            , jsonObject);
                }
            }
            // Application
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

                    PackageManager packageManager = getPackageManager();

                    ApplicationInfo appInfo;
                    try {
                        appInfo = packageManager.getApplicationInfo(event.getPackageName().toString(), PackageManager.GET_META_DATA);
                    } catch (PackageManager.NameNotFoundException | NullPointerException | Resources.NotFoundException e) {
                        appInfo = null;
                    }
                    PackageInfo pkgInfo;
                    try {
                        pkgInfo = packageManager.getPackageInfo(event.getPackageName().toString(), PackageManager.GET_META_DATA);
                    } catch (PackageManager.NameNotFoundException | NullPointerException | Resources.NotFoundException e) {
                        pkgInfo = null;
                    }

                    String appName = "";
                    try {
                        if (appInfo != null) {
                            appName = packageManager.getApplicationLabel(appInfo).toString();
                            
                        }
                    } catch (Resources.NotFoundException | NullPointerException e) {
                        appName = "";
                    }

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("Status", "Foreground");
                        jsonObject.put("PackageName", event.getPackageName().toString());
                        jsonObject.put("AppName", appName);
                        if (pkgInfo != null)
                            jsonObject.put("SystemApp", isSystemPackage(pkgInfo));
                        jsonObject.put("Timestamp", System.currentTimeMillis() / 1000);
                        Log.i("CHANGE", "APPLICATION (Foreground) " + jsonObject);
                        prev = System.currentTimeMillis();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                new FileWriter().addData(filePath, FileWriter.DATA_TYPE.APPLICATIONS,
                        new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                        , jsonObject);

                }
            }

    }

    /**
     * Check if a certain application is pre-installed or part of the operating system.
     *
     * @param {@link PackageInfo} obj
     * @return boolean
     */
    public static boolean isSystemPackage(PackageInfo pkgInfo) {
        return pkgInfo != null && ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null)
            filePath = intent.getStringExtra("filePath");
        return START_STICKY;
    }

    @Override
    public void onInterrupt() {

    }
}
