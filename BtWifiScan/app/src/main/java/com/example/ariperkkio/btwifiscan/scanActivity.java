package com.example.ariperkkio.btwifiscan;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class scanActivity extends Activity implements View.OnClickListener, ListView.OnItemClickListener, LocationListener {

    // General
    private Button endScan; // 'End scanning' button
    private Button save; // 'Save' button
    private Button dontSave; // 'Don't save' button
    private Button map;
    private TextView scanName;
    private TextView scanFoundBt;
    private TextView scanFoundWifi;
    private TextView scanLocation;
    private Intent intent;
    private ListView scanResultList;
    private customAdapter listAdapter;

    // Database
    private databaseManager database;
    private saveResultsBackground backgroundSaver;
    private ProgressDialog progressDialog;

    // Scanning
    private int sampleRate;
    private scanWithSampleRate backgroundScanner;
    // Wifi
    private WifiManager wifiManager;
    private List<ScanResult> wifiScanResults; //
    private int numberOfWifiNetworks;
    // Bluetooth
    private BluetoothAdapter btAdapter; //btAdapter for accessing bluetooth
    private BluetoothDevice btDevice;  //btDevice for getting discovered devices' info
    private int btDeviceRSSI; // btDeviceRSSI for getting RSSI from
    private boolean mReceiverRegistered = false; // my work-around for checking if broadcast receiver is registered
    private ArrayList<scanResult> scanresults; // List for bt and wifi scan results
    private int numberOfBtDevices;

    // Options
    // Bluetooth
    private boolean btStatus;
    private boolean btDevName;
    private boolean btDevAddr;
    private boolean btDevType;
    private boolean btRSSI;
    // Wifi
    private boolean wifiStatus;
    private boolean wifiSSID;
    private boolean wifiBSSID;
    private boolean wifiCapabilities;
    private boolean wifiFrequency;
    private boolean wifiRSSI;

    // Location
    private LocationManager locationManager;
    private String latitude = "0";
    private String longitude = "0";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //Set full screen
        setContentView(R.layout.activity_scan);

        // Button initializing
        endScan = (Button) findViewById(R.id.scanEnd);
        endScan.setOnClickListener(this);
        save = (Button) findViewById(R.id.scanSave);
        save.setOnClickListener(this);
        dontSave = (Button) findViewById(R.id.scanDontSave);
        dontSave.setOnClickListener(this);
        save.setVisibility(View.GONE); //Not visible until scan ended
        dontSave.setVisibility(View.GONE); //Not visible until scan ended
        map = (Button) findViewById(R.id.ScanMap);
        map.setOnClickListener(this);

        // Text Fields
        scanName = (TextView) findViewById(R.id.ScanName);
        scanFoundBt = (TextView) findViewById(R.id.ScanBtFound);
        scanFoundWifi = (TextView) findViewById(R.id.ScanWifiFound);
        scanLocation = (TextView) findViewById(R.id.ScanLocation);

        // Scanning objects
        btAdapter = BluetoothAdapter.getDefaultAdapter(); // Get local bluetooth adapter
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

        // Database and List objects
        database = new databaseManager(this);
        scanresults = new ArrayList<scanResult>();
        scanResultList = (ListView) findViewById(R.id.ScanList);
        scanResultList.setOnItemClickListener(scanActivity.this);
        listAdapter = new customAdapter(this, scanresults);
        scanResultList.setAdapter(listAdapter);

        backgroundSaver = new saveResultsBackground();
        backgroundScanner = new scanWithSampleRate();
        setUpLocation();
    }

    // BroadcastReceiver to receive data from btDiscrovery
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); // get found action into string
            if (BluetoothDevice.ACTION_FOUND.equals(action)) { // This is called every time bluetooth device is discovered
                btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); // Get the BluetoothDevice object from the Intent
                btDeviceRSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);  // Get RSSI value from intent

                // These attributes are set to default every time bt device is discovered
                String deviceName = "";
                String address = "";
                int type = 4; // type 4 is initialized as "" in scanResult class

                // Check options and get requested attributes. By ignoring one, it keeps its default value initialized above
                if (btDevName)
                    deviceName = btDevice.getName();
                if (btDevAddr)
                    address = btDevice.getAddress();
                if (btDevType)
                    type = btDevice.getType();
                if (!btRSSI) // if not requested attribute, set it to zero. Otherwise keep its value
                    btDeviceRSSI = 0;

                if (!checkIfScanned(btDevice.getAddress())) { // Check duplicates based on address
                    Log.e("BtADD", "Adding new BtDevice");
                    // Create new scanResult with correct constructor, add object to List
                    try{
                        scanresults.add(new scanResult(deviceName, address, type, btDeviceRSSI, latitude+" ,"+longitude));
                        listAdapter.notifyDataSetChanged(); // Refresh list
                        numberOfBtDevices++; // Increase number of devices
                        scanFoundBt.setText(numberOfBtDevices + ""); // Update text with new number of devices
                    } catch (IllegalStateException error){
                        Log.e("Bluetooth add: ", error.toString());
                    }
                }
            }
        }
    }; //End BroadcastReceiver

    private void setUpLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, sampleRate*500, 1, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, sampleRate*500, 1, this);
            progressDialog = new ProgressDialog(scanActivity.this);
            progressDialog.setMessage("Getting location...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false); // Force dialog show (disable click responsive)
        }
    }

    // LocationListeners methods
    public void onProviderEnabled(String provider) { }
    public void onProviderDisabled(String provider) { }
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    public void onLocationChanged(Location location){

        if (latitude.equals("0") && longitude.equals("0")) // Initial launch
        {
            progressDialog.hide();
            backgroundScanner.execute(sampleRate); // Start scanning in background. New sample every samplerate time
        }
        if (location != null){
            latitude = "" + location.getLatitude();
            longitude = "" + location.getLongitude();
            scanLocation.setText("(" + String.format("%.4f", location.getLatitude()) + ", " + String.format("%.4f", location.getLongitude()) + ")");
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.scanEnd): // End scan button
                backgroundScanner.cancel(true); // End background scanning
                save.setVisibility(View.VISIBLE); // Show 'Save' button
                dontSave.setVisibility(View.VISIBLE); // Show "Don't save" button
                endScan.setVisibility(View.GONE); // Hide 'End scan' button
                if(btAdapter.isDiscovering())
                    btAdapter.cancelDiscovery(); // End bt scanning if its still running
            break;

            case (R.id.scanSave):
                backgroundSaver.execute(scanresults); // Save results in another thread
            break;

            case (R.id.scanDontSave): // Go back to mainActivity
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Close previous activities
                startActivity(intent);
            break;

            case (R.id.ScanMap):
                // TODO: Use Parcelable
                intent = new Intent(getApplicationContext(), scanMap.class);
                intent.putExtra("Count", scanresults.size());
                for(int i = 0;i<scanresults.size();i++)
                    intent.putExtra("location "+i, scanresults.get(i).getLocation());
                startActivity(intent);

            break;
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View v, int position, long arg) {
        scanResult selectedObject = (scanResult) (scanResultList.getItemAtPosition(position));
        intent = new Intent(scanActivity.this, resultDetailsActivity.class);

        // Fill in details of selected scan
        intent.putExtra("technology", selectedObject.getTechnology());
        intent.putExtra("location", selectedObject.getLocation());

        intent.putExtra("btDevName", selectedObject.getBtDevName());
        intent.putExtra("btDevAddr", selectedObject.getBtDevAddr());
        intent.putExtra("btDevType", selectedObject.getBtDevType());
        intent.putExtra("btRSSI", Integer.toString(selectedObject.getBtRSSI())); //resultDetailsActivity handles this as string

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
            btAdapter.startDiscovery(); // Start scanning Bt devices
            // Register mReceiver to run in UI thread - allows updating Views
            // Set receiver to be called by broadcast intents matching BtDevice.ACTION_FOUND
            registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            mReceiverRegistered = true; // Broadcast receiver registered
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
        return false; // No match found
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
                            publishProgress(new scanResult(SSID, BSSID, capabilities, frequency, RSSI, latitude+" ,"+longitude));
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

    // Background result saving with progress bar
    private class saveResultsBackground extends AsyncTask<List<scanResult>, Integer, Void> {

        // Before saving show progress dialog
        // Runs on UI thread - View updating allowed
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(scanActivity.this);
            progressDialog.setMessage("Saving results");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(false); //Set progress updates visible
            progressDialog.setMax(scanresults.size());
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false); // Force dialog show (disable click responsive)
        }
        // After saving
        protected void onPostExecute(Void value) {
            // Refresh result history on mainActivity when finishing this one
            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        // Save results and update progress dialog after each insert
        protected Void doInBackground(List<scanResult>... resultList) {
            try {
                database.open();
                database.insertIntoScans(getIntent().getExtras().getString("scanName")); // Creates new scan with auto incremented scanId -> highest Id is latest scan
                int scanId = database.getHighestId(); // Get latest scanId

                for (int i = 0; i < resultList[0].size(); i++) {
                    if (resultList[0].get(i).getTechnology().equals("Bluetooth"))
                        database.insertIntoBtResults(scanId, resultList[0].get(i).getBtDevName(),
                                resultList[0].get(i).getBtDevAddr(), resultList[0].get(i).getBtDevType(),
                                resultList[0].get(i).getBtRSSI(), resultList[0].get(i).getLocation());
                    if (resultList[0].get(i).getTechnology().equals("Wifi")) {
                        database.insertIntoWifiResults(scanId, resultList[0].get(i).getWifiSSID(),
                                resultList[0].get(i).getWifiBSSID(), resultList[0].get(i).getWifiCapabilities(),
                                resultList[0].get(i).getWifiFrequency(), resultList[0].get(i).getWifiRSSI(), resultList[0].get(i).getLocation());
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

