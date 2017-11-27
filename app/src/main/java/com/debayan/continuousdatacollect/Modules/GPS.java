package com.debayan.continuousdatacollect.Modules;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.debayan.continuousdatacollect.Utils.FileWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by debayan on 10/16/17.
 */

public class GPS implements LocationListener {

    private LocationManager locationManager;
    private FileWriter fileWriter;
    private String filePath;

    // Flag for GPS status
    boolean isGPSEnabled = false;

    // Flag for network status
    boolean isNetworkEnabled = false;

    // Flag for GPS status
    boolean canGetLocation = false;


    Location location; // Location
    double latitude; // Latitude
    double longitude; // Longitude

    public void setFilePath(String fp) {
        filePath = fp;
    }

    public  Location getLocation(Context context, String fp, FileWriter fw, int GPS_SAMPLING_RATE, int MIN_DISTANCE) {
        filePath = fp;
        fileWriter = fw;
        try {
            locationManager = (LocationManager) context
                    .getSystemService(LOCATION_SERVICE);

            // Getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // Getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // No network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {

                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.

                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            GPS_SAMPLING_RATE,
                            MIN_DISTANCE, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // If GPS enabled, get latitude/longitude using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                GPS_SAMPLING_RATE,
                                MIN_DISTANCE, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            JSONObject gpsTrace = new JSONObject();
            gpsTrace.put("Lat", getLatitude());
            gpsTrace.put("Lon", getLongitude());
            gpsTrace.put("Timestamp", System.currentTimeMillis() / 1000);
            Log.i("CHANGE", "GPS " + gpsTrace);
            fileWriter.addData(filePath,
                    FileWriter.DATA_TYPE.GPS,
                    new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                    , gpsTrace);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return location;
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }


    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    @Override
    public void onLocationChanged(Location newLocation) {

        if (location.getLatitude() != newLocation.getLatitude() ||
                location.getLongitude() != newLocation.getLongitude()) {
            Log.i("CHANGE", "LOCATION: " + newLocation.toString() + " from " + location.toString());
            location = newLocation;
            try {
                JSONObject gpsTrace = new JSONObject();
                gpsTrace.put("Lat", getLatitude());
                gpsTrace.put("Lon", getLongitude());
                gpsTrace.put("Timestamp", System.currentTimeMillis() / 1000);
                fileWriter.addData(filePath,
                        FileWriter.DATA_TYPE.GPS,
                        new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                        , gpsTrace);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


}
