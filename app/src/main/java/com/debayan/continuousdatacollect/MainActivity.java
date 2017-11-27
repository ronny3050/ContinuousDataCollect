package com.debayan.continuousdatacollect;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;

import static com.debayan.continuousdatacollect.R.string.accessibility_service;

public class MainActivity extends Activity {


    public static final int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        Log.i("PREFERENCE-REGISTERED", String.valueOf(sharedPreferences.getBoolean("REGISTERED", false)));
        if (!sharedPreferences.getBoolean("REGISTERED", false)) {
            Intent intent = new Intent(this, com.debayan.continuousdatacollect.RegistrationActivity.class);
            startActivityForResult(intent, 5);
        } else {
            showInstallScreenOrStartDataCollection();
        }

    }

    public boolean isAccessibilityEnabled() {

        String TAG = "accessible";
        int accessibilityEnabled = 0;
        final String service = getPackageName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    getApplicationContext().getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }

        if (accessibilityEnabled == 1) {
            return true;
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_ALL:
                if (grantResults.length > 0) {
                    // Start Data Collection service and quit
/*                    Intent intent = new Intent(getApplicationContext(), DataCollectionService.class);
                    getApplicationContext().startService(intent);
                    finish();*/
                }
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("Permission", permission);
                    return false;
                }
            }
        }
        return true;
    }

    public void onInstallBtnClicked(View v) {


        AssetManager assetManager = getAssets();

        InputStream in = null;
        OutputStream out = null;

        try {
            in = assetManager.open("continuous_keyboard.apk");
            out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/continuous_keyboard.apk");

            byte[] buffer = new byte[1024];

            int read;
            while ((read = in.read(buffer)) != -1) {

                out.write(buffer, 0, read);

            }

            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;

            Intent intent = new Intent(Intent.ACTION_VIEW);

            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/continuous_keyboard.apk")),
                    "application/vnd.android.package-archive");
            startActivityForResult(intent, 400);
            //finish();

        } catch (Exception e) {
            Log.e("CONTINUOUS_KEYBOARD", String.valueOf(e));
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 200: {
                showInstallScreenOrStartDataCollection();
                break;
            }

            case 5: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.System.canWrite(getApplicationContext())) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getApplicationContext().getPackageName()));
                        startActivity(intent);
                    }

                }
                if (!hasPermissions(this, PERMISSIONS)) {
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
                    if (!isAccessibilityEnabled()) {
                        Log.i("Accessibility", "Off");

                        final Activity that = this;
                        new MaterialDialog.Builder(this)
                                .title(accessibility_service)
                                .content(R.string.accessibility_content)
                                .positiveText(R.string.proceed)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                        startActivityForResult(intent, 200);
                                    }
                                })
                                .show();
                    }

                } else {
                    showInstallScreenOrStartDataCollection();
                }
                break;
            }

            case 400:

                new MaterialDialog.Builder(this)
                        .title("ContinuousKeyboard")
                        .content("We request you to use ContinuousKeyboard for typing for the remainder of the study. \n1. Please click on the text box below and" +
                                " choose the keyboard icon on the bottom left corner. \n2.Then click 'CHOOSE KEYBOARDS' and choose 'ContinuousKeyboard'." +
                                "\n3. Then return back to this screen and click keyboard icon on the bottom left corner and choose 'ContinuousKeyboard'. \nThis is the last step, we will not bother you again. :)")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("Test Here", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                            }
                        })
                        .positiveText(R.string.proceed)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                showInstallScreenOrStartDataCollection();
                            }
                        }).show();

        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void showInstallScreenOrStartDataCollection() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        List<ApplicationInfo> installedApps = new ArrayList<ApplicationInfo>();

        for (ApplicationInfo app : apps) {
            //checks for flags; if flagged, check if updated system app
            if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                installedApps.add(app);
                //it's a system app, not interested
            } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                //Discard this one
                //in this case, it should be a user-installed app
            } else {
                installedApps.add(app);
            }
        }
        boolean keyboardAppFound = false;
        for (ApplicationInfo app : installedApps) {
            Log.i("APPPP", app.packageName);
            if (Objects.equals(app.packageName, "com.debayan.android.continuouskeyboard")) {
                keyboardAppFound = true;
                break;
            }
        }

        // Check if keyboard has been installed
        if (keyboardAppFound) {
            findViewById(R.id.install_btn).setVisibility(View.GONE);
            findViewById(R.id.install_txt).setVisibility(View.GONE);
            findViewById(R.id.progress_bar_2).setVisibility(View.VISIBLE);
            findViewById(R.id.timeLeft).setVisibility(View.VISIBLE);

            RingProgressBar mRingProgressBar = (RingProgressBar) findViewById(R.id.progress_bar_2);

            // Set the progress bar's progress
            SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
            long currentTime = System.currentTimeMillis();
            long firstUpload = sharedPreferences.getLong("FIRST_UPLOAD", currentTime);
            long endTime = firstUpload + 1000 * 60 * 60 * 24 * 15;
            float progress = ((float) (currentTime - firstUpload) / (float) (1000 * 60 * 60 * 24 * 15)) * 100.0f;

            if (progress < 0f)
                progress = 0f;
            if (progress > 100f)
                progress = 100f;

            if (System.currentTimeMillis() > endTime) {
                ((TextView) findViewById(R.id.timeLeft)).setText("You have successfully completed the data collection! We really appreciate your patience and time. Please contact Debayan at debdebay@msu.edu or text (517)-930-0792 to obtain $50 cash!");
            } else {
                // Start Data Collection service and quit
                // if not already running data collection
                Log.e("RUNNING", String.valueOf(isMyServiceRunning(com.debayan.continuousdatacollect.DataCollectionService.class)));
                if (!isMyServiceRunning(com.debayan.continuousdatacollect.DataCollectionService.class)) {
                    Intent intent = new Intent(getApplicationContext(), com.debayan.continuousdatacollect.DataCollectionService.class);
                    getApplicationContext().startService(intent);
                }

                new CountDownTimer(endTime - currentTime, 1000) {
                    public void onTick(long millisUntilFinished) {
                        float seconds = millisUntilFinished % 1000;
                        millisUntilFinished /= 60 * 1000;
                        float minutes = millisUntilFinished % 60;
                        millisUntilFinished /= 60;
                        float hours = millisUntilFinished % 24;
                        millisUntilFinished /= 24;
                        float days = millisUntilFinished;
                        ((TextView) findViewById(R.id.timeLeft)).setText((int) days + " Days " + (int) hours + " Hours  " + (int) minutes + " Minutes of Data Collection Left.\nThank you for your patience!\nYou may close the app.");
                    }

                    public void onFinish() {
                        ((TextView) findViewById(R.id.timeLeft)).setText("You have successfully completed the data collection! We really appreciate your patience and time. Please contact Debayan at debdebay@msu.edu or text (517)-930-0792 to obtain $50 cash!\nYou may close the app.");
                    }
                }.start();
            }

            mRingProgressBar.setProgress((int) progress);
            mRingProgressBar.setOnProgressListener(new RingProgressBar.OnProgressListener() {

                @Override
                public void progressToComplete() {
                    // Progress reaches the maximum callback default Max value is 100
                    Toast.makeText(MainActivity.this, "complete", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            findViewById(R.id.install_btn).setVisibility(View.VISIBLE);
            findViewById(R.id.install_txt).setVisibility(View.VISIBLE);
        }
    }

}
