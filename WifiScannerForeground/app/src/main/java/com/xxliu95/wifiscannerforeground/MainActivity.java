package com.xxliu95.wifiscannerforeground;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WifiScanner";

    private static final int REQUEST_CODE_PERMISSIONS = 200;

    private static final String KEY_PERMISSIONS_REQUEST_COUNT = "KEY_PERMISSIONS_REQUEST_COUNT";
    private static final int MAX_NUMBER_REQUEST_PERMISSIONS = 2;

    private static final List<String> sPermissions = Arrays.asList(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
    );

    private int mPermissionRequestCount;

    private WifiManager wifiManager;
    private ListView listView;
    private Button scanButton;
    private int size = 0;
    private List<ScanResult> results;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PERMISSIONS_REQUEST_COUNT, mPermissionRequestCount);
    }

    private void requestPermissionsIfNecessary() {
        if (!checkAllPermissions()) {
            if (mPermissionRequestCount < MAX_NUMBER_REQUEST_PERMISSIONS) {
                mPermissionRequestCount += 1;
                ActivityCompat.requestPermissions(
                        this,
                        sPermissions.toArray(new String[0]),
                        REQUEST_CODE_PERMISSIONS);
            } else {
                finish();
            }
        }
    }

    private boolean checkAllPermissions() {
        boolean hasPermissions = true;
        for (String permission : sPermissions) {
            hasPermissions &=
                    ContextCompat.checkSelfPermission(
                            this, permission) == PackageManager.PERMISSION_GRANTED;
        }
        return hasPermissions;
    }

    /** Permission Checking **/

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            requestPermissionsIfNecessary(); // no-op if permissions are granted already.
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mPermissionRequestCount =
                    savedInstanceState.getInt(KEY_PERMISSIONS_REQUEST_COUNT, 0);
        }

        requestPermissionsIfNecessary();

        scanButton = (Button) findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanWifi();
            }
        });

        listView = (ListView) findViewById(R.id.wifiList);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);

        scanWifi();
    }

    private void scanWifi() {
        arrayList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        Log.d(TAG, "scanWifi: scanning");
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults();
            unregisterReceiver(this);

            Log.d(TAG, "scanWifi: received");

            for (ScanResult scanResult : results) {
                arrayList.add(scanResult.SSID + " - " + scanResult.capabilities);
                adapter.notifyDataSetChanged();
            }
        }
    };
}
