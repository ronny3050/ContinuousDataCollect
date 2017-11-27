package com.debayan.continuousdatacollect;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.debayan.continuousdatacollect.Modules.Accelerometer;
import com.debayan.continuousdatacollect.Modules.Applications;
import com.debayan.continuousdatacollect.Modules.Battery;
import com.debayan.continuousdatacollect.Modules.Bluetooth;
import com.debayan.continuousdatacollect.Modules.Brightness;
import com.debayan.continuousdatacollect.Modules.Cell;
import com.debayan.continuousdatacollect.Modules.GPS;
import com.debayan.continuousdatacollect.Modules.Gravity;
import com.debayan.continuousdatacollect.Modules.Gyroscope;
import com.debayan.continuousdatacollect.Modules.Heart;
import com.debayan.continuousdatacollect.Modules.Humidity;
import com.debayan.continuousdatacollect.Modules.Light;
import com.debayan.continuousdatacollect.Modules.LinearAccelerometer;
import com.debayan.continuousdatacollect.Modules.Magnetometer;
import com.debayan.continuousdatacollect.Modules.Pressure;
import com.debayan.continuousdatacollect.Modules.Proximity;
import com.debayan.continuousdatacollect.Modules.Rotation;
import com.debayan.continuousdatacollect.Modules.Screen;
import com.debayan.continuousdatacollect.Modules.Step;
import com.debayan.continuousdatacollect.Modules.Temperature;
import com.debayan.continuousdatacollect.Modules.Volume;
import com.debayan.continuousdatacollect.Modules.WiFi;
import com.debayan.continuousdatacollect.Utils.Constants;
import com.debayan.continuousdatacollect.Utils.FileWriter;
import com.debayan.continuousdatacollect.Utils.UploadUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by debayan on 10/16/17.
 */

public class DataCollectionService extends Service  {
    private static final String TAG = "DataCollectionService";

    public FileWriter fileWriter;
    public String filePath;
    private int FILE_CNT = 1;
    private boolean waiting = false;

    /* GPS PARAMETERS */
    private final int MIN_DISTANCE = 0; // (every move)
    private final int GPS_SAMPLING_RATE = 1000 * 60 * 1; // (every minute)

    /* MAGNETOMETER PARAMETERS */
    private final float MAGNETOMETER_CHANGE = 3f; // (every 10 degree change)
    private final int MAGNETOMETER_FREQUENCY = 1000000;

    /* ACCELEROMETER PARAMETERS */
    private final int ACCELEROMETER_FREQUENCY = 200000;
    private final float ACCELEROMETER_CHANGE = 1.5f;

    /* LINEAR-ACCELEROMETER PARAMETERS */
    private final int LINEAR_ACCELEROMETER_FREQUENCY = 1000000;
    private final float LINEAR_ACCELEROMETER_CHANGE = 1f;

    /* GRAVITY PARAMETERS */
    private final int GRAVITY_FREQUENCY = 1000000;
    private final float GRAVITY_CHANGE = 0.1f;

    /* GRAVITY PARAMETERS */
    private final int ROTATION_FREQUENCY = 1000000;
    private final float ROTATION_CHANGE = 0.01f;

    /* GYROSCOPE PARAMETERS */
    private final int GYROSCOPE_FREQUENCY = 1000000;
    private final float GYROSCOPE_CHANGE = 0.1f; // (every 10 degree change)

    /* BLUETOOTH PARAMETERS */
    private final int BLUETOOTH_SAMPLING_RATE = 1000 * 60 * 20; // (every 20 minutes)

    /* WIFI PARAMETERS */
    private final int WIFI_SAMPLING_RATE = 1000 * 60 * 20; // (every 20 minutes)

    /* LIGHTSENSOR PARAMETERS */
    private final int LIGHT_FREQUENCY = 100000;
    private final int LIGHT_CHANGE = 10;

    /* TEMPERATURE PARAMETERS */
    private final int TEMPERATURE_FREQUENCY = 1000000;
    private final int TEMPERATURE_CHANGE = 3;

    /* PRESSURE PARAMETERS */
    private final int PRESSURE_CHANGE = 5;

    /* HUMIDITY PARAMETERS */
    private final int HUMIDITY_FREQUENCY = 1000000;
    private final int HUMIDITY_CHANGE = 10;

    /* PROXIMITY PARAMETERS */
    private final int PROXIMITY_FREQUENCY = 1000000;

    /* HEART PARAMETERS */
    private final int HEART_CHANGE = 10;

    /* BRIGHTNESS PARAMETERS */
    private final int BRIGHTNESS_SAMPLING_RATE = 1000;

    /* UPLOAD PARAMETERS */
    private final int UPLOAD_PERIOD = 60*1000;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        Toast.makeText(this, "Data Collection Stopped", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");
    }

    public GPS gps;
    public Bluetooth bluetooth;
    public Cell cell;
    public Accelerometer accelerometer;
    public LinearAccelerometer linAccelerometer;
    public Gravity gravity;
    public Battery battery;
    public Brightness brightness;
    public Gyroscope gyroscope;
    public Heart heart;
    public Humidity humidity;
    public Light light;
    public Magnetometer magnetometer;
    public Pressure pressure;
    public Proximity proximity;
    public Rotation rotation;
    public Screen screen;
    public Step step;
    public Temperature temperature;
    public Volume volume;
    public WiFi wiFi;

    private TransferUtility transferUtility;

    @Override
    public void onStart(Intent intent, int startid)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),  MODE_PRIVATE);
        FILE_CNT = sharedPreferences.getInt("FILE_CNT", 1);

        transferUtility = UploadUtils.getTransferUtility(getApplicationContext());

        Toast.makeText(this, "Data Collection Started. You may close the app.", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onStart");
        fileWriter = new FileWriter();

        // Upload info file
        if(fileWriter.initialize(getApplicationContext())) {
            String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ContinuousAuthentication/";
            new File(baseDir).mkdirs();
            String info =  android_id + "-info" + ".profile";
            String infoFP = baseDir + File.separator + info;




            if (infoFP == null) {
                Toast.makeText(this, "Could not find the filepath of the selected file",
                        Toast.LENGTH_LONG).show();
                return;
            }
            // Duplicate file for upload
            final File file = new File(infoFP);

            String name = sharedPreferences.getString("NAME", "");
            String email = sharedPreferences.getString("EMAIL", "");

            transferUtility.upload(Constants.BUCKET_NAME, android_id + "-" + name + "-" + email + "/" +
                            file.getName(),
                    file);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("LAST_UPLOAD", System.currentTimeMillis());
            editor.putInt("FILE_CNT", 1);
            editor.putLong("FIRST_UPLOAD", System.currentTimeMillis());
            FILE_CNT = 1;
            editor.apply();
            writeFileCount(FILE_CNT);
        }

        String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ContinuousAuthentication/";
        String fileName =  android_id + "-" +
                new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()) + "-" +
                FILE_CNT + ".profile";
        filePath = baseDir + File.separator + fileName;

        // Get list of all sensors available for the phone
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensor = sensorManager.getSensorList(Sensor.TYPE_ALL);

        GPSCapture(getApplicationContext());
        BluetoothCapture(getApplicationContext());
        MagnetometerCapture(getApplicationContext());
        AccelerometerCapture(getApplicationContext());
        LinearAccelerometerCapture(getApplicationContext());
        GravityCapture(getApplicationContext());
        GyroscopeCapture(getApplicationContext());
        BatteryCapture(getApplicationContext());
        WiFiCapture(getApplicationContext());
        LightCapture(getApplicationContext());
        ProximityCapture(getApplicationContext());
        BrightnessCapture(getApplicationContext());
        CellCapture(getApplicationContext());
        RotationCapture(getApplicationContext());
        // Temperature
        TemperatureCapture(getApplicationContext()); // Not tested
        // Pressure
        PressureCapture(getApplicationContext());
        // Humidity
        HumidityCapture(getApplicationContext());    // Not tested
        // Heart
        HeartCapture(getApplicationContext());      // Not tested
        // Step
        StepCapture(getApplicationContext());
        // Volume
        VolumeCapture(getApplicationContext());
        // Screen (on, off)
        ScreenCapture(getApplicationContext());
//        // App History
        Intent newintent = new Intent(getApplicationContext(), Applications.class);
        newintent.putExtra("filePath",filePath);
        startService(newintent);

//        // Face Image

//        CognitoCachingCredentialsProvider sCredProvider = new CognitoCachingCredentialsProvider(
//                getApplicationContext(),
//                Constants.COGNITO_POOL_ID,
//                Regions.fromName(Constants.COGNITO_POOL_REGION));
//
//        // Create an S3 client
//        AmazonS3Client sS3Client = new AmazonS3Client(sCredProvider);
//        sS3Client.setRegion(Region.getRegion(Regions.fromName(Constants.BUCKET_REGION)));


//     Log.i("TRANS", "TIME TO UPLOA2");
        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        long currentTime = System.currentTimeMillis();
        Long lastUpload = sharedPreferences.getLong("LAST_UPLOAD", currentTime);
        if(currentTime - lastUpload >= UPLOAD_PERIOD) {
            beginUpload();
        } else {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i("TRANS", "TIME TO UPLOA2");
                    SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),  MODE_PRIVATE);
                    long currentTime = System.currentTimeMillis();
                    Long lastUpload = sharedPreferences.getLong("LAST_UPLOAD", currentTime);
                    if (currentTime - lastUpload >= UPLOAD_PERIOD) {
                        // Time to upload :)
                        beginUpload();
                    }
                }
            }, UPLOAD_PERIOD);
        }

    }



    private void writeFileCount(int fc) {
        try {
            java.io.FileWriter fileWriter = new java.io.FileWriter(new File(Environment.getExternalStorageDirectory() + "/ContinuousAuthentication/fc.txt"));
            fileWriter.write(String.valueOf(fc));
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFile(String inputFile, String outputFile) {

        InputStream in = null;
        OutputStream out = null;
        try {


            in = new FileInputStream(inputFile);
            out = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        }  catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    private void DeleteFile(String inputFile) {
        try {
            // delete the original file
            new File(inputFile).delete();
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    private void cancelTransaction(int id) {
        transferUtility.cancel(id);
    }

    /*
     * Begins to upload the file specified by the file path.
     */
    public void beginUpload() {

        if (filePath == null) {
            Toast.makeText(this, "Could not find the filepath of the selected file",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (!new File(filePath).exists()) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i("TRANS", "TIME TO UPLOAD");
                    SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),  MODE_PRIVATE);
                    long currentTime = System.currentTimeMillis();
                    Long lastUpload = sharedPreferences.getLong("LAST_UPLOAD", currentTime);
                    if(currentTime - lastUpload >= UPLOAD_PERIOD) {
                        // Time to upload :)
                        beginUpload();
                    }
                }
            }, UPLOAD_PERIOD);
            return;
        }
        // Duplicate file for upload
        copyFile(filePath, filePath + "_dup");
        final File file = new File(filePath + "_dup");
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),  MODE_PRIVATE);
        String name = sharedPreferences.getString("NAME", "");
        String email = sharedPreferences.getString("EMAIL", "");

        TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME,
                Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID) + "-" + name + "-" + email + "/" +
                        file.getName().substring(0, file.getName().lastIndexOf('_')),
                        file);


        /*
         * Note that usually we set the transfer listener after initializing the
         * transfer. However it isn't required in this sample app. The flow is
         * click upload button -> start an activity for image selection
         * startActivityForResult -> onActivityResult -> beginUpload -> onResume
         * -> set listeners to in progress transfers.
         */
        // observer.setTransferListener(new UploadListener());
        observer.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                // do something
                Log.i("TRANS", id + " , " + state.name());
                if(state.name().equals("WAITING_FOR_NETWORK")) {
                    waiting = true;
                }
                if(state.name().equals("IN_PROGRESS")) {
                    if(waiting) {
                        waiting = false;
                        cancelTransaction(id);
                        beginUpload();
                    }
                }
                if(state.name().equals("COMPLETED")) {
                    SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),  MODE_PRIVATE);
                    String name = sharedPreferences.getString("NAME", "");
                    String email = sharedPreferences.getString("EMAIL", "");
                    final File keyFile = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ContinuousAuthentication/" +
                            Settings.Secure.getString(getApplicationContext().getContentResolver(),
                                    Settings.Secure.ANDROID_ID) + "-keys_" + String.valueOf(sharedPreferences.getInt("FILE_CNT", 1)) +  ".profile");
                    if(keyFile.exists()) {
                        Log.i("TRANS-key", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID) + "-" + name + "-" + email + "/");
                        transferUtility.upload(Constants.BUCKET_NAME,
                                Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID) + "-" + name + "-" + email + "/" +
                                        keyFile.getName(), keyFile).setTransferListener(new TransferListener() {
                            @Override
                            public void onStateChanged(int id, TransferState state) {
                                if(state.name().equals("COMPLETED")) {
                                    DeleteFile(keyFile.getAbsolutePath());
                                }
                            }

                            @Override
                            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

                            }

                            @Override
                            public void onError(int id, Exception ex) {
                            }
                        });


                    }
//
//                    final File faceFile = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ContinuousAuthentication/" +
//                            Settings.Secure.getString(getApplicationContext().getContentResolver(),
//                                    Settings.Secure.ANDROID_ID) + "-face_" + String.valueOf(sharedPreferences.getInt("FILE_CNT", 1)) +  ".profile");
//                    if(faceFile.exists()) {
//                        Log.i("TRANS-face", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID) + "-" + name + "-" + email + "/");
//                        transferUtility.upload(Constants.BUCKET_NAME,
//                                Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID) + "-" + name + "-" + email + "/" +
//                                        faceFile.getName(), faceFile).setTransferListener(new TransferListener() {
//                            @Override
//                            public void onStateChanged(int id, TransferState state) {
//                                if(state.name().equals("COMPLETED")) {
//                                    DeleteFile(faceFile.getAbsolutePath());
//                                }
//                            }
//
//                            @Override
//                            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
//
//                            }
//
//                            @Override
//                            public void onError(int id, Exception ex) {
//                            }
//                        });
//
//
//                    }

                    DeleteFile(filePath + "_dup");
                    sharedPreferences = getSharedPreferences(getPackageName(),  MODE_PRIVATE);
                    FILE_CNT ++;

                    writeFileCount(FILE_CNT);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong("LAST_UPLOAD", System.currentTimeMillis());
                    editor.putInt("FILE_CNT", FILE_CNT);
                    editor.apply();

                    String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                    String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ContinuousAuthentication/";
                    String fileName =  android_id + "-" +
                            new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()) + "-" +
                            FILE_CNT + ".profile";
                    String oldFilePath = filePath;
                    filePath = baseDir + File.separator + fileName;
                    gps.setFilePath(filePath);
                    cell.setFilePath(filePath);
                    Intent intent = new Intent(getApplicationContext(), Applications.class);
                    intent.putExtra("filePath",filePath);
                    startService(intent);
                    bluetooth.setFilePath(filePath);
                    accelerometer.setFilePath(filePath);
                    linAccelerometer.setFilePath(filePath);
                    gravity.setFilePath(filePath);
                    battery.setFilePath(filePath);
                    brightness.setFilePath(filePath);
                    gyroscope.setFilePath(filePath);
                    heart.setFilePath(filePath);
                    humidity.setFilePath(filePath);
                    light.setFilePath(filePath);
                    magnetometer.setFilePath(filePath);
                    pressure.setFilePath(filePath);
                    proximity.setFilePath(filePath);
                    screen.setFilePath(filePath);
                    step.setFilePath(filePath);
                    temperature.setFilePath(filePath);
                    volume.setFilePath(filePath);
                    wiFi.setFilePath(filePath);
                    rotation.setFilePath(filePath);
                    DeleteFile(oldFilePath);

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("TRANS", "TIME TO UPLOAD");
                            SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),  MODE_PRIVATE);
                            long currentTime = System.currentTimeMillis();
                            Long lastUpload = sharedPreferences.getLong("LAST_UPLOAD", currentTime);
                            if(currentTime - lastUpload >= UPLOAD_PERIOD) {
                                // Time to upload :)
                                beginUpload();
                            }
                        }
                    }, UPLOAD_PERIOD);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                //Display percentage transfered to user
            }

            @Override
            public void onError(int id, Exception ex) {
                // do something
                beginUpload();
            }
        });
    }


    private void GPSCapture(final Context context) {
        gps =new GPS();
        gps.getLocation(context, filePath, fileWriter, GPS_SAMPLING_RATE, MIN_DISTANCE);

    }

    private void MagnetometerCapture(Context context){
       magnetometer = new Magnetometer();
        magnetometer.initialize(context, filePath, fileWriter, MAGNETOMETER_FREQUENCY, MAGNETOMETER_CHANGE);
    }

    private void RotationCapture(Context context){
        rotation = new Rotation();
        rotation.initialize(context, filePath, fileWriter, ROTATION_FREQUENCY, ROTATION_CHANGE);
    }

    private void CellCapture(Context context){
        cell = new Cell();
        cell.initialize(context, filePath, fileWriter);
    }

    private void AccelerometerCapture(Context context) {
        accelerometer = new Accelerometer();
        accelerometer.initialize(context, filePath, fileWriter, ACCELEROMETER_FREQUENCY ,ACCELEROMETER_CHANGE);
    }

    private void GravityCapture(Context context) {
        gravity = new Gravity();
        gravity.initialize(context, filePath, fileWriter, GRAVITY_FREQUENCY ,GRAVITY_CHANGE);
    }

    private void LinearAccelerometerCapture(Context context) {
        linAccelerometer = new LinearAccelerometer();
        linAccelerometer.initialize(context, filePath, fileWriter, LINEAR_ACCELEROMETER_FREQUENCY , LINEAR_ACCELEROMETER_CHANGE);
    }

    private void GyroscopeCapture(Context context) {
        gyroscope = new Gyroscope();
        gyroscope.initialize(context, filePath, fileWriter, GYROSCOPE_FREQUENCY, GYROSCOPE_CHANGE);
    }

    private void BluetoothCapture(Context context) {
        bluetooth = new Bluetooth();
        bluetooth.initialize(context, filePath, fileWriter, BLUETOOTH_SAMPLING_RATE);
    }

    private void BatteryCapture(Context context) {
        battery = new Battery();
        battery.initialize(context, filePath, fileWriter);
    }

    private void WiFiCapture(Context context) {
        wiFi = new WiFi();
        wiFi.initialize(context, filePath, fileWriter, WIFI_SAMPLING_RATE);
    }

    private void LightCapture(Context context) {
        light = new Light();
        light.initialize(context, filePath, fileWriter, LIGHT_FREQUENCY, LIGHT_CHANGE);
    }

    private void ProximityCapture(Context context) {
        proximity = new Proximity();
        proximity.initialize(context, filePath, fileWriter, PROXIMITY_FREQUENCY);
    }

    private void BrightnessCapture(Context context) {
        brightness = new Brightness();
        brightness.initialize(context, filePath, fileWriter, BRIGHTNESS_SAMPLING_RATE);
    }

    private void TemperatureCapture(Context context) {
        temperature = new Temperature();
        temperature.initialize(context, filePath, fileWriter, TEMPERATURE_FREQUENCY, TEMPERATURE_CHANGE);
    }

    private void PressureCapture(Context context) {
        pressure = new Pressure();
        pressure.initialize(context, filePath, fileWriter, PRESSURE_CHANGE);
    }

    private void HumidityCapture(Context context) {
        humidity = new Humidity();
        humidity.initialize(context, filePath, fileWriter, HUMIDITY_FREQUENCY, HUMIDITY_CHANGE);
    }

    private void HeartCapture(Context context) {
        heart = new Heart();
        heart.initialize(context, filePath, fileWriter, HEART_CHANGE);
    }

    private void VolumeCapture(Context context) {
        volume = new Volume();
        volume.initialize(context, filePath, fileWriter);
    }

    private void ScreenCapture(Context context) {
        screen = new Screen();
        screen.initialize(context, filePath, fileWriter);
    }

    private void StepCapture(Context context) {
        step = new Step();
        step.initialize(context, filePath, fileWriter);
    }
}
