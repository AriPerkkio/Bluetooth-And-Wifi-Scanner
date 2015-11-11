package com.example.ariperkkio.btwifiscan;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class scanActivity extends Activity implements View.OnClickListener, ListView.OnItemClickListener {

    // General widgets
    private Button endScan; // 'End scanning' button
    private Button save; // 'Save' button
    private Button dontSave; // 'Don't save' button
    private TextView scanName;
    private TextView scanFoundBt;
    private TextView scanFoundWifi;
    private databaseManager database;
    private Intent intent;
    private scanWithSampleRate backgroundScanner;
    private saveResultsBackground backgroundSaver;
    private ProgressDialog progressDialog;

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

        save = (Button) findViewById(R.id.scanSave);
        save.setOnClickListener(this);

        dontSave = (Button) findViewById(R.id.scanDontSave);
        dontSave.setOnClickListener(this);

        save.setVisibility(View.GONE);
        dontSave.setVisibility(View.GONE);

        scanName = (TextView) findViewById(R.id.ScanName);
        scanFoundBt = (TextView) findViewById(R.id.ScanBtFound);
        scanFoundWifi = (TextView) findViewById(R.id.ScanWifiFound);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        wifiManager = (WifiManager) this.getSystemService(this.WIFI_SERVICE);

        // Get options for Bluetooth scanning
        if (btStatus = getIntent().getExtras().getBoolean("btStatus")) {
            btDevName = getIntent().getExtras().getBoolean("btDevName");
            btDevAddr = getIntent().getExtras().getBoolean("btDevAddr");
            btDevType = getIntent().getExtras().getBoolean("btDevType");
            btRSSI = getIntent().getExtras().getBoolean("btRSSI");
        }
        // Get options for Wifi scanning
        if (wifiStatus = getIntent().getExtras().getBoolean("wifiStatus")) {
            wifiSSID = getIntent().getExtras().getBoolean("wifiSSID");
            wifiBSSID = getIntent().getExtras().getBoolean("wifiBSSID");
            wifiCapabilities = getIntent().getExtras().getBoolean("wifiCapabilities");
            wifiFrequency = getIntent().getExtras().getBoolean("wifiFrequency");
            wifiRSSI = getIntent().getExtras().getBoolean("wifiRSSI");
        }
        // Get scan name and sample rate
        scanName.setText(getIntent().getExtras().getString("scanName"));
        sampleRate = getIntent().getExtras().getInt("sampleRate");

        database = new databaseManager(this);

        scanresults = new ArrayList<scanResult>();

        //for(int i=0; i<3000;i++)
        //    scanresults.add(new scanResult("SSID", "BSSID "+i, "Capabilities", i*10, 100+i));

        scanResultList = (ListView) findViewById(R.id.ScanList);
        scanResultList.setOnItemClickListener(scanActivity.this);

        listAdapter = new customAdapter(this, scanresults);
        scanResultList.setAdapter(listAdapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving results");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false); //Set progress updates visible

        backgroundSaver = new saveResultsBackground();

        backgroundScanner = new scanWithSampleRate();
        backgroundScanner.execute(sampleRate); // Start scanning in background. New sample every samplerate time
    }

    // BroadcastReceiver to receive data from btDiscrovery
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); // get found action into string
            if (BluetoothDevice.ACTION_FOUND.equals(action)) { // This is called every time bluetooth device is discovered
                btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); // Get the BluetoothDevice object from the Intent
                btDeviceRSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);  // Get RSSI value from intent
                mReceiverRegistered = true; // Broadcast receiver registered

                // These attributes are set to default every time bt device is discovered
                String deviceName = "";
                String address = "";
                int type = 4; // type 4 is initialized as "" in scanResult

                // Check options and get requested attributes. By ignoring one, it keeps its default value initialized above
                if (btDevName)
                    deviceName = btDevice.getName();
                if (btDevAddr)
                    address = btDevice.getAddress();
                if (btDevType)
                    type = btDevice.getType();
                if (!btRSSI) // if not requested attribute, set it to zero. Otherwise keep its value
                    btDeviceRSSI = 0;

                // Check duplicates based on address
                if (!checkIfScanned(btDevice.getAddress())) {
                    Log.e("BtADD", "Adding new BtDevice");
                    // Create new scanResult with correct constructor, add object to List
                    try{
                        scanresults.add(new scanResult(deviceName, address, type, btDeviceRSSI));
                        listAdapter.notifyDataSetChanged();
                        numberOfBtDevices++; // Increase number of devices
                        scanFoundBt.setText(numberOfBtDevices + ""); // Update text with new number of devices
                    } catch (IllegalStateException error){
                        Log.e("Bluetooth add: ", error.toString());
                    }
                }
            }
        }
    }; //End BroadcastReceiver

    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.scanEnd): // End scan button
                backgroundScanner.cancel(true);
                save.setVisibility(View.VISIBLE);
                dontSave.setVisibility(View.VISIBLE);
                endScan.setVisibility(View.GONE);
                btAdapter.cancelDiscovery(); // This may be temporary solution, fix by threading scan
            break;

            case (R.id.scanSave):
                backgroundSaver.execute(scanresults);
            break;

            case (R.id.scanDontSave):
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            break;
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View v, int position, long arg) {
        scanResult selectedObject = (scanResult) (scanResultList.getItemAtPosition(position));
        intent = new Intent(scanActivity.this, resultDetailsActivity.class);

        // Fill in details of selected scan
        intent.putExtra("technology", selectedObject.getTechnology());
        intent.putExtra("btDevName", selectedObject.getBtDevName());
        intent.putExtra("btDevAddr", selectedObject.getBtDevAddr());
        intent.putExtra("btDevType", selectedObject.getBtDevType());
        intent.putExtra("btRSSI", Integer.toString(selectedObject.getBtRSSI())); //resultDetailsActivity handles this as string

        intent.putExtra("technology", selectedObject.getTechnology());
        intent.putExtra("wifiSSID", selectedObject.getWifiSSID());
        intent.putExtra("wifiBSSID", selectedObject.getWifiBSSID());
        intent.putExtra("wifiCapabilities", selectedObject.getWifiCapabilities());
        intent.putExtra("wifiFrequency", Integer.toString(selectedObject.getWifiFrequency())); //resultDetailsActivity handles this as string
        intent.putExtra("wifiRSSI", Integer.toString(selectedObject.getWifiRSSI())); //resultDetailsActivity handles this as string

        startActivity(intent); // Start resultDetailsActivity but keep this one alive
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister broadcast receiver from receiving bt discoveries
        if (mReceiverRegistered)// my work-around for checking if broadcast receiver is registered
            unregisterReceiver(mReceiver);
    }

    // This is called from AsyncTask
    private void scanBt() {
        if (btAdapter.isDiscovering()) //Should never occur since min sampleRate is over 12 sec
            btAdapter.cancelDiscovery();
        else {
            btAdapter.startDiscovery();
            registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    // Filter out devices which have already been scanned
    // NOTE: Ignoring a device is only based on address. When scanning without address there will be duplicates.
    public boolean checkIfScanned(String address) {
        for (int i = 0; i < scanresults.size(); i++) {
            if (scanresults.get(i).getTechnology().equals("Bluetooth"))
                if (scanresults.get(i).getBtDevAddr().equals(address))
                    return true;
            if (scanresults.get(i).getTechnology().equals("Wifi"))
                if (scanresults.get(i).getWifiBSSID().equals(address))
                    return true;
        }
        return false;
    }

    // Background scanning
    // Wifi scanning runs on AsyncTask, Bt scanning is only started from here
    // When new wifi is found publishProgress is used to pass scanResult object to UI thread
    // where it is added to list (and adapter notified).
    // BtDevice discoveries are still done by broadcast receiver
    private class scanWithSampleRate extends AsyncTask<Integer, scanResult, Void> {

        // Loop scanning
        protected Void doInBackground(Integer... integers) {
            long sampleRate = integers[0] * 1000; // Convert samplerate from seconds to ms

            while (!isCancelled()) { // as long as called .cancel(true)
                if (btStatus) // Check if bt option was selected in newSCanActivity
                    scanBt(); // Starts discovery
                if (wifiStatus) { // Check if wifi option was selected in newSCanActivity
                    wifiManager.startScan(); // Scans wifi networks. Scanning is over immediately
                    wifiScanResults = wifiManager.getScanResults(); // Get results from previous wifi scan
                    // Loop through all the results
                    for (int i = 0; i < wifiScanResults.size(); i++) {
                        // These values are set for a scanResult if specific attribute wasn't selected to be scanned
                        String SSID = "";
                        String BSSID = "";
                        String capabilities = "";
                        int frequency = 0;
                        int RSSI = 0;

                        // Check options and get requested attributes. By ignoring one, it keeps its default value initialized above
                        if (wifiSSID)
                            SSID = wifiScanResults.get(i).SSID;
                        if (wifiBSSID)
                            BSSID = wifiScanResults.get(i).BSSID;
                        if (wifiCapabilities)
                            capabilities = wifiScanResults.get(i).capabilities;
                        if (wifiFrequency)
                            frequency = wifiScanResults.get(i).frequency;
                        if (wifiRSSI)
                            RSSI = wifiScanResults.get(i).level;
                        // Check duplicates based on address
                        if (!checkIfScanned(wifiScanResults.get(i).BSSID)) {
                            numberOfWifiNetworks++;
                            // Pass new scanResult wifiNetwork to UI thread
                            publishProgress(new scanResult(SSID, BSSID, capabilities, frequency, RSSI));
                        }
                    }
                } //Scanning done
                try {
                    Thread.sleep(sampleRate); // Stand-by for sampleRate (s)
                } catch (InterruptedException e) {
                    Log.e("SampleRateSleep: ", e.toString());
                }
            }
            return null;
        }
        @Override // Runs on UI thread. All View updates must be done here.
        protected void onProgressUpdate(scanResult... values) {
                scanresults.add(values[0]); //Add object to list in UI thread
                listAdapter.notifyDataSetChanged(); // Data changed in scanresults - refresh list
                scanFoundWifi.setText(numberOfWifiNetworks + ""); // Update number of wifiNetworks found
        }
    } // Scanning AsyncTask close

    // ToDo: When saving, rest of UI should not response to clicks
    // Background result saving with progress bar
    private class saveResultsBackground extends AsyncTask<List<scanResult>, Integer, Void> {

        protected void onPreExecute() {
            progressDialog.setMax(scanresults.size());
            progressDialog.show();
        }

        protected void onPostExecute(Void value) {
            finish();
        }

        protected Void doInBackground(List<scanResult>... resultList) {
            try {
                database.open();
                database.insertIntoScans(getIntent().getExtras().getString("scanName")); // Creates new scan with auto incremented scanId -> highest Id is latest scan
                int scanId = database.getHighestId(); // Get latest scanId

                for (int i = 0; i < resultList[0].size(); i++) {
                    if (resultList[0].get(i).getTechnology().equals("Bluetooth"))
                        database.insertIntoBtResults(scanId, resultList[0].get(i).getBtDevName(),
                                resultList[0].get(i).getBtDevAddr(), resultList[0].get(i).getBtDevType(), resultList[0].get(i).getBtRSSI());
                    if (resultList[0].get(i).getTechnology().equals("Wifi")) {
                        database.insertIntoWifiResults(scanId, resultList[0].get(i).getWifiSSID(),
                                resultList[0].get(i).getWifiBSSID(), resultList[0].get(i).getWifiCapabilities(),
                                resultList[0].get(i).getWifiFrequency(), resultList[0].get(i).getWifiRSSI());
                    }
                    publishProgress(i); //Update current progress
                }
                database.close();
            } catch (SQLException e) {
                Log.e("InsertIntoScans", e.toString());
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setProgress(progress[0]);
        }

    }
}

