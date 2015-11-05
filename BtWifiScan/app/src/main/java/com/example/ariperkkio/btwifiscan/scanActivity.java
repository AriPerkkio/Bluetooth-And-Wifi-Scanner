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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class scanActivity extends Activity implements View.OnClickListener{

    // General widgets
    private Button endScan; // 'End scanning' button
    private TextView scanName;
    private TextView scanFoundBt;
    private TextView scanFoundWifi;
    private databaseManager database;

    // Bluetooth scanning objects
    private BluetoothAdapter btAdapter; //btAdapter for accessing bluetooth
    private BluetoothDevice btDevice;  //btDevice for getting discovered devices' info
    private int btDeviceRSSI; // btDeviceRSSI for getting RSSI from
    private boolean mReceiverRegistered = false; // my work-around for checking if broadcast receiver is registered
    private ArrayList<scanResult> scanresults; // List for bt and wifi scan results
    private int numberOfBtDevices;


    //Wifi scanning objects
    private WifiManager wifiManager;
    private List<ScanResult> wifiScanResults; //
    private int numberOfWifiNetworks;

    private int sampleRate;

    // booleans to check options for bluetooth
    private boolean btStatus;
    private boolean btDevName;
    private boolean btDevAddr;
    private boolean btDevType;
    private boolean btRSSI;

    // booleans to check options for wifi
    private boolean wifiStatus;
    private boolean wifiSSID;
    private boolean wifiBSSID;
    private boolean wifiCapabilities;
    private boolean wifiFrequency;
    private boolean wifiRSSI;

    // Button for manual scanning, will be removed later
    private Button manualButton;

    // List items
    private ListView scanResultList;
    private customAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //Set full screen
        setContentView(R.layout.activity_scan);

        endScan = (Button) findViewById(R.id.scanEnd);
        endScan.setOnClickListener(this);

        scanName = (TextView) findViewById(R.id.ScanName);
        scanFoundBt = (TextView) findViewById(R.id.ScanBtFound);
        scanFoundWifi = (TextView) findViewById(R.id.ScanWifiFound);

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

        database = new databaseManager(this);

        scanresults = new ArrayList<scanResult>();
        scanresults.add(new scanResult("Test Device with long name ABCDEFG", "12:34:56:78:90", 3, -88));
        scanresults.add(new scanResult("Test Wifi network with very long name ABCDEFG","12:34:56:78:90", "[WPA][AES256]", 2445 , -85));

        scanResultList = (ListView) findViewById(R.id.ScanList);
        listAdapter = new customAdapter(this, scanresults);
        scanResultList.setAdapter(listAdapter);
    }

    // BroadcastReceiver to receive data from btDiscrovery
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); // get found action into string
            if (BluetoothDevice.ACTION_FOUND.equals(action)) { // This is called every time bluetooth device is discovered
                btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); // Get the BluetoothDevice object from the Intent
                btDeviceRSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);  // Get RSSI value from intent
                mReceiverRegistered = true; // Broadcast receiver registered

                //ToDo:     Loop through scanned devices and check address from all objects
                //ToDo      If already scanned that device, skip it -> No duplicates

                // These attributes are set to default every time bt device is discovered
                String deviceName = "";
                String address = "";
                int type = 4; // type 4 is initialized as "" in scanResult

                // Check options and get requested attributes. By ignoring one, it keeps its default value initialized above
                if(btDevName)
                    deviceName = btDevice.getName();
                if(btDevAddr)
                    address = btDevice.getAddress();
                if(btDevType)
                    type = btDevice.getType();
                if(!btRSSI) // if not requested attribute, set it to zero. Otherwise keep its value
                    btDeviceRSSI = 0;

                // Create new scanResult with correct constructor, add object to List
                scanresults.add(new scanResult(deviceName, address, type, btDeviceRSSI));
                listAdapter.notifyDataSetChanged(); // Tell adapter to refresh itself
                numberOfBtDevices++; // Increase number of devices
                scanFoundBt.setText(numberOfBtDevices+""); // Update text with new number of devices
            }
        }
    }; //End BroadcastReceiver

    public void onClick(View v) {
        switch(v.getId()) {
            case (R.id.scanEnd): // End scan button
                // Todo: Don't finish activity. Hide Button and bring Save / Don't save visible.
                // Todo  Enable onItemClickListener (may require flag to see if scanning ended)
                try {
                    database.open();
                    database.insertIntoScans(getIntent().getExtras().getString("scanName"));
                    database.close();
                }catch (SQLException e) {
                    Log.e("InsertIntoScans", e.toString());
                }
                finish();
            break;

            case (R.id.newScanManual): //Temp button, will be replaced to be set by timer
                if(wifiStatus)
                    scanWifi();
                if(btStatus)
                    scanBt();
            break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister broadcast receiver from receiving bt discoveries
        if(mReceiverRegistered)// my work-around for checking if broadcast receiver is registered
            unregisterReceiver(mReceiver);
    }

    private void scanWifi() {
        //Todo: if(btDeviceName) -> btDeviceName = btDevice.getName() (by default it's ""

        wifiManager.startScan();
        wifiScanResults = wifiManager.getScanResults();

        //ToDo:     Loop through scanned networks and check address from all objects
        //ToDo      If already scanned that network, skip it -> No duplicates

        for (int i = 0; i < wifiScanResults.size(); i++) {

            // These attributes are set to default at every for loop
            String SSID = "";
            String BSSID = "";
            String capabilities = "";
            int frequency = 0;
            int RSSI = 0;

            // Check options and get requested attributes. By ignoring one, it keeps its default value initialized above
            if(wifiSSID)
                SSID = wifiScanResults.get(i).SSID;
            if(wifiBSSID)
                BSSID = wifiScanResults.get(i).BSSID;
            if(wifiCapabilities)
                capabilities = wifiScanResults.get(i).capabilities;
            if(wifiFrequency)
                frequency = wifiScanResults.get(i).frequency;
            if(wifiRSSI)
                RSSI = wifiScanResults.get(i).level;
            // Create object and add it to list
            scanresults.add(new scanResult(SSID, BSSID, capabilities, frequency, RSSI));
            numberOfWifiNetworks++;
            scanFoundWifi.setText(numberOfWifiNetworks+"");
        }
        listAdapter.notifyDataSetChanged(); // tell adapter to update itself
    }

    private void scanBt() {
        if(btAdapter.isDiscovering()) //This may not be needed when timer starts scanning
            btAdapter.cancelDiscovery();
        else {
            btAdapter.startDiscovery();
            registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }
}
