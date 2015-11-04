package com.example.ariperkkio.btwifiscan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Vector;

public class scanActivity extends Activity implements View.OnClickListener{

    private Button back;
    // Bluetooth scanning objects
    private BluetoothAdapter btAdapter;
    private BluetoothDevice btDevice;

    //Wifi scanning objects
    private WifiManager wifiManager;
    private List<ScanResult> wifiScanResults;

    private TextView scanName;
    private int sampleRate;

    // booleans to check options for bluetooth
    private boolean btStatus;
    private boolean btDevName;
    private boolean btDevAddr;
    private boolean btDevType;
    private boolean btRSSI;

    private int btDeviceRSSI;

    // booleans to check options for wifi
    private boolean wifiStatus;
    private boolean wifiSSID;
    private boolean wifiBSSID;
    private boolean wifiCapabilities;
    private boolean wifiFrequency;
    private boolean wifiRSSI;

    private Button manualButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //Set full screen
        setContentView(R.layout.activity_scan);

        back = (Button) findViewById(R.id.scanEnd);
        back.setOnClickListener(this);

        scanName = (TextView) findViewById(R.id.ScanName);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        wifiManager = (WifiManager) this.getSystemService(this.WIFI_SERVICE);

        // Get options for Bluetooth scanning
        if(btStatus = getIntent().getExtras().getBoolean("btStatus")) {
            btDevName = getIntent().getExtras().getBoolean("btDevName");
            btDevAddr = getIntent().getExtras().getBoolean("btDevAddr");
            btDevType = getIntent().getExtras().getBoolean("btDevType");
            btRSSI = getIntent().getExtras().getBoolean("btRSSI");
        }
        // Get options for Wifi scanning
        if(wifiStatus = getIntent().getExtras().getBoolean("wifiStatus")) {
            wifiSSID = getIntent().getExtras().getBoolean("wifiSSID");
            wifiBSSID = getIntent().getExtras().getBoolean("wifiBSSID");
            wifiCapabilities = getIntent().getExtras().getBoolean("wifiCapabilities");
            wifiFrequency = getIntent().getExtras().getBoolean("wifiFrequency");
            wifiRSSI = getIntent().getExtras().getBoolean("wifiRSSI");
        }
        // Get scan name and sample rate
        scanName.setText(getIntent().getExtras().getString("scanName"));
        sampleRate = getIntent().getExtras().getInt("sampleRate");

        manualButton = (Button) findViewById(R.id.newScanManual);
        manualButton.setOnClickListener(this);

    }

    // BroadcastReceiver to receive data from btDiscrovery
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btDeviceRSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
            }
        }
    };

    public void onClick(View v) {
        switch(v.getId()) {
            case (R.id.scanEnd):
                finish();
            break;

            case (R.id.newScanManual): //Temp button, will be replaced to be set by timer

                wifiManager.startScan();
                wifiScanResults = wifiManager.getScanResults();

                if(btAdapter.isDiscovering())
                    btAdapter.cancelDiscovery();

                else {
                    btAdapter.startDiscovery();
                    registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
                }
            break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

}
