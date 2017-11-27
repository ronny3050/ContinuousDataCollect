package com.debayan.continuousdatacollect.Modules;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.debayan.continuousdatacollect.Utils.FileWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by debayan on 10/18/17.
 */

public class Cell {
    private static FileWriter fileWriter;
    private static String filePath;
    private static TelephonyManager telephonyManager;

    public void initialize(final Context context, String fp, FileWriter fw) {
        fileWriter = fw;
        filePath = fp;
        telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(new TelephonyState(), PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    /**
     * Tracks cell location and telephony information:
     * - GSM: CID, LAC, PSC (UMTS Primary Scrambling Code)
     * - CDMA: base station ID, Latitude, Longitude, Network ID, System ID
     * - Telephony: IMEI/MEID/ESN, software version, line number, network MMC, network code, network name, network type, phone type, sim code, sim operator, sim serial, subscriber ID
     *
     * @author df
     */
    public static class TelephonyState extends PhoneStateListener {

        private SignalStrength lastSignalStrength;

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            lastSignalStrength = signalStrength;
        }

        @Override
        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);

            if (lastSignalStrength == null) return;

            if (location instanceof GsmCellLocation) {
                GsmCellLocation loc = (GsmCellLocation) location;


                JSONObject cellTrace = new JSONObject();

                try {
                    cellTrace.put("CID", loc.getCid());
                    cellTrace.put("LAC", loc.getLac());
                    cellTrace.put("PSC", loc.getPsc());
                    cellTrace.put("Strength", lastSignalStrength);
                    cellTrace.put("GSM_BER", lastSignalStrength.getGsmBitErrorRate());
                    cellTrace.put("Timestamp", System.currentTimeMillis() / 1000);
                    Log.i("CHANGE", "GSM ");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                fileWriter.addData(filePath, FileWriter.DATA_TYPE.CELL,
                        new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                        , cellTrace);


            } else {
                CdmaCellLocation loc = (CdmaCellLocation) location;

                JSONObject cellTrace = new JSONObject();

                try {
                    cellTrace.put("BASE_STATION_ID", loc.getBaseStationId());
                    cellTrace.put("BASE_STATION_LATITUDE", loc.getBaseStationLatitude());
                    cellTrace.put("BASE_STATION_LONGITUDE", loc.getBaseStationLongitude());
                    cellTrace.put("NETWORK_ID", loc.getNetworkId());
                    cellTrace.put("SYSTEM_ID", loc.getSystemId());
                    cellTrace.put("SIGNAL_STRENGTH", lastSignalStrength.getCdmaDbm());
                    cellTrace.put("CDMA_ECIO", lastSignalStrength.getCdmaEcio());
                    cellTrace.put("EVDO_DBM", lastSignalStrength.getEvdoDbm());
                    cellTrace.put("EVDO_ECIO", lastSignalStrength.getEvdoEcio());
                    cellTrace.put("EVDO_SNR", lastSignalStrength.getEvdoSnr());
                    cellTrace.put("Timestamp", System.currentTimeMillis() / 1000);
                } catch (Exception e) {
                }
                fileWriter.addData(filePath, FileWriter.DATA_TYPE.CELL,
                        new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())
                        , cellTrace);
            }
        }

    }

    public void setFilePath(String fp) {
        filePath = fp;
    }
}
