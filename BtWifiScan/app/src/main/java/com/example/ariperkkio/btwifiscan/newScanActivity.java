package com.example.ariperkkio.btwifiscan;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class newScanActivity extends Activity implements View.OnClickListener {

    private Intent intent;
    private BluetoothAdapter btAdapter;
    int REQUEST_ENABLE_BT;
    private WifiManager wifiManager;
    private LocationManager locationManager;


    //General widgets
    private Button back;
    private Button startScan;
    private EditText scanName;
    private NumberPicker sampleRate;

    //Widgets for Bluetooth options:
    private Switch switchBluetooth;
    private CheckBox btDevName;
    private CheckBox btDevAddr;
    private CheckBox btDevType;
    private CheckBox btRSSI;
    private TextView btEnabled;
    private Intent enableBtIntent;

    //Widgets for Wifi options
    private Switch switchWifi;
    private CheckBox wifiSSID;
    private CheckBox wifiBSSID;
    private CheckBox wifiCapabilities;
    private CheckBox wifiRSSI;
    private CheckBox wifiFrequency;
    private TextView wifiEnabled;

    //Widgets for Location options
    private Switch switchLocation;
    private CheckBox locGps;
    private CheckBox locNetwork;

    //Widgets for Global Database option
    private Switch switchGdb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //Set full screen
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); //Hide automatic keyboard
        setContentView(R.layout.activity_new_scan);

        //General widgets
        back = (Button) findViewById(R.id.newScanBack);
        back.setOnClickListener(this);
        startScan = (Button) findViewById(R.id.newScanStartScan);
        startScan.setOnClickListener(this);
        scanName = (EditText) findViewById(R.id.ScanName);
        sampleRate = (NumberPicker) findViewById(R.id.newScanRateNumber);
        sampleRate.setMinValue(15); //Bt discovery timeouts after 12sec
        sampleRate.setMaxValue(900);

        // Widgets for Bluetooth
        switchBluetooth = (Switch) findViewById(R.id.newScanBtSwitch);
        switchBluetooth.setOnClickListener(this);
        btDevName = (CheckBox) findViewById(R.id.newScanDevNameChk);
        btDevName.setOnClickListener(this);
        btDevAddr = (CheckBox) findViewById(R.id.newScanDevAddrChk);
        btDevAddr.setOnClickListener(this);
        btDevType = (CheckBox) findViewById(R.id.newScanDevTypChk);
        btDevType.setOnClickListener(this);
        btRSSI = (CheckBox) findViewById(R.id.newScanBtRssiChk);
        btRSSI.setOnClickListener(this);
        btEnabled = (TextView) findViewById(R.id.newScanBtStatus);
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // Widgets for Wifi
        switchWifi = (Switch) findViewById(R.id.newScanWifiSwitch);
        switchWifi.setOnClickListener(this);
        wifiSSID = (CheckBox) findViewById(R.id.newScanWifiSsidChk);
        wifiSSID.setOnClickListener(this);
        wifiBSSID = (CheckBox) findViewById(R.id.newScanWifiBssidChk);
        wifiBSSID.setOnClickListener(this);
        wifiCapabilities = (CheckBox) findViewById(R.id.newScanWifiCapaChk);
        wifiCapabilities.setOnClickListener(this);
        wifiRSSI = (CheckBox) findViewById(R.id.newScanWifiRssiChk);
        wifiRSSI.setOnClickListener(this);
        wifiFrequency = (CheckBox) findViewById(R.id.newScanWifiFreqChk);
        wifiFrequency.setOnClickListener(this);
        wifiEnabled = (TextView) findViewById(R.id.newScanWifiStatus);
        wifiManager = (WifiManager) this.getSystemService(this.WIFI_SERVICE);

        // Widgets for Location
        switchLocation = (Switch) findViewById(R.id.newScanLocSwitch);
        switchLocation.setOnClickListener(this);
        locGps = (CheckBox) findViewById(R.id.newScanGpsChk);
        locGps.setOnClickListener(this);
        locNetwork = (CheckBox) findViewById(R.id.newScanNetChk);
        locNetwork.setOnClickListener(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Widgets for Global Database
        switchGdb = (Switch) findViewById(R.id.newScanGdbSwitch);
        switchGdb.setOnClickListener(this);

        // Check if device's bluetooth is ON when activity launched
        if (btAdapter != null && btAdapter.isEnabled())
            btEnabled.setText("Enabled");
        else {
            btEnabled.setText("Disabled");
            setBtOptionsOFF(); // Set bluetooth option checkboxes off
            switchBluetooth.setChecked(false); // Uncheck bluetooth option switch
        }
        // Check if device's wifi is ON when activity launched
        if (wifiManager.isWifiEnabled())
            wifiEnabled.setText("Enabled");
        else {
            wifiEnabled.setText("Disabled");
            setWifiOptionsOFF();
            switchWifi.setChecked(false);
        }
        // Check if device's location is available when activity launched
        if(locationManager != null) {
            switchLocation.setChecked(true);
            setLocationOptionsOFF(); // First set all OFF, then check if available and check ON
            setLocationOptionsON();
        }
        else {
            switchLocation.setChecked(false);
            setLocationOptionsOFF();
        }
    }

    @Override // Check which option user selected on 'Enable Bluetooth' request
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT){
            if (resultCode == RESULT_OK){ // User clicked OK
                switchBluetooth.setChecked(true);
                setBtOptionsON();
                btEnabled.setText("Enabled");
            }
            if(resultCode == RESULT_CANCELED){ // User clicked Cancel
                switchBluetooth.setChecked(false);
                setBtOptionsOFF();
                btEnabled.setText("Disabled");
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {

            case (R.id.newScanBack):  // 'Back' button to finish activity
                finish();
            break;

            case (R.id.newScanBtSwitch): // Switch to enable/disable options for bluetooth
                // Check if device's bluetooth is enabled/disabled
                if (btAdapter != null && !btAdapter.isEnabled()) { //disabled -> prompt user to enable Bt
                    enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); //Result of selection to REQUEST_ENABLE_BT
                }
                // Enable/disable check boxes depending on switch state
                if(switchBluetooth.isChecked() && btAdapter != null &&  btAdapter.isEnabled())
                    setBtOptionsON();
                else {
                    setBtOptionsOFF();
                    switchBluetooth.setChecked(false);
                }
                if(btAdapter == null)
                    Toast.makeText(this, "Unable to enable bluetooth. Please enable it manually.", Toast.LENGTH_SHORT).show();
            break;

            // Switch to enable/disable options for wifi
            case (R.id.newScanWifiSwitch):
                // Check if device's wifi is enabled/disabled
                if(wifiManager != null && !wifiManager.isWifiEnabled()){
                    wifiManager.setWifiEnabled(true); // Turn wifi ON
                    Toast.makeText(this, "Wifi enabled.", Toast.LENGTH_SHORT).show();
                    switchWifi.setChecked(true);
                    setWifiOptionsON();
                    wifiEnabled.setText("Enabled");
                }
                // Enable/disable check boxes depending on switch state
                if(switchWifi.isChecked() && wifiManager != null && wifiManager.isWifiEnabled())
                    setWifiOptionsON();
                else {
                    setWifiOptionsOFF();
                    switchWifi.setChecked(false);
                }
                if(wifiManager==null)
                    Toast.makeText(this, "Unable to enable wifi. Please enable it manually.", Toast.LENGTH_SHORT).show();
            break;

            case (R.id.newScanLocSwitch):
                if (locationManager == null) {
                    Toast.makeText(this, "Unable to use location", Toast.LENGTH_SHORT).show();
                    switchLocation.setChecked(false);
                    setLocationOptionsOFF();
                }
                if(switchLocation.isChecked() && locationManager != null)
                    setLocationOptionsON();
                else {
                    setLocationOptionsOFF();
                    switchLocation.setChecked(false);
                }
            break;

            case (R.id.newScanGdbSwitch):
                // Global Database results required all settings enabled
                if(switchGdb.isChecked()){
                    if(switchWifi.isChecked())
                        setWifiOptionsON();
                    if(switchBluetooth.isChecked())
                        setBtOptionsON();
                    if(!switchLocation.isChecked())
                        switchLocation.performClick();
                }
                // Check if settings were successfully enabled
                if(!switchLocation.isChecked())
                    switchGdb.setChecked(false);
            break;

            // 'Start scanning' button to start new activity with chosen options
            // Scanning requires at least one option checked and scan name
            case (R.id.newScanStartScan):
                // Check that either bluetooth or wifi switch is checked
                if(!switchWifi.isChecked() && !switchBluetooth.isChecked()) {
                    Toast.makeText(this, "Please check at least one technology", Toast.LENGTH_SHORT).show();
                    break;
                }
                // For bluetooth scanning: Check that there is at least one Bluetooth option checked
                else if(switchBluetooth.isChecked() && !btDevName.isChecked() && !btDevAddr.isChecked() && !btDevType.isChecked() && !btRSSI.isChecked()) {
                    Toast.makeText(this, "Please check Bluetooth options", Toast.LENGTH_SHORT).show();
                    break;
                }
                // For wifi scanning: Check that there is at least one wifi option checked
                else if(switchWifi.isChecked() && !wifiSSID.isChecked() && !wifiBSSID.isChecked() && !wifiCapabilities.isChecked() && !wifiFrequency.isChecked() && !wifiRSSI.isChecked()) {
                    Toast.makeText(this, "Please check Wifi options", Toast.LENGTH_SHORT).show();
                    break;
                }
                // For location: Check that there is at least one location provider option checked
                else if(switchLocation.isChecked() && !locGps.isChecked() && !locNetwork.isChecked()){
                    Toast.makeText(this, "Please check Location options", Toast.LENGTH_SHORT).show();
                    break;
                }
                // For Global Database: All the options of each technology has to be enabled. Location has to be enabled
                else if(switchGdb.isChecked() && !switchLocation.isChecked()) {
                    Toast.makeText(this, "Location must be enabled when using Global Database.", Toast.LENGTH_SHORT).show();
                    break;
                }
                else if(switchGdb.isChecked() && switchWifi.isChecked() &&
                        (!wifiSSID.isChecked() || !wifiBSSID.isChecked() || !wifiCapabilities.isChecked() || !wifiFrequency.isChecked() || !wifiRSSI.isChecked())){
                    Toast.makeText(this, "All Wifi options must be checked when using Global Database.", Toast.LENGTH_SHORT).show();
                    break;
                }
                else if(switchGdb.isChecked() && switchBluetooth.isChecked() &&
                        (!btDevName.isChecked() || !btDevAddr.isChecked() || !btDevType.isChecked() || !btRSSI.isChecked())){
                    Toast.makeText(this, "All Bluetooth options must be checked when using Global Database.", Toast.LENGTH_SHORT).show();
                    break;
                }
                // Scan name must be inserted
                else if(scanName.length() <= 0 ){
                    Toast.makeText(this, "Please insert scan name", Toast.LENGTH_SHORT).show();
                    break;
                }
                else if(scanName.getText().toString().equals("Global Database")){
                    Toast.makeText(this, "Scan name cannot be "+scanName.getText().toString()+". Please rename scan. ", Toast.LENGTH_SHORT).show();
                    break;
                }
                else { // OK, gather options
                    intent = new Intent(newScanActivity.this, scanActivity.class); //Create intent for scanActivity

                    // Fill in Sample Rate and Scan Name
                    intent.putExtra("sampleRate", sampleRate.getValue());
                    intent.putExtra("scanName", scanName.getText().toString());

                    // Fill in options for bluetooth scanning
                    intent.putExtra("btStatus", switchBluetooth.isChecked());
                    if(switchBluetooth.isChecked()) {
                        intent.putExtra("btDevName", btDevName.isChecked());
                        intent.putExtra("btDevAddr", btDevAddr.isChecked());
                        intent.putExtra("btDevType", btDevType.isChecked());
                        intent.putExtra("btRSSI", btRSSI.isChecked());
                    }
                    // Fill in options for wifi scanning
                    intent.putExtra("wifiStatus", switchWifi.isChecked());
                    if(switchWifi.isChecked()) {
                        intent.putExtra("wifiSSID", wifiSSID.isChecked());
                        intent.putExtra("wifiBSSID", wifiBSSID.isChecked());
                        intent.putExtra("wifiCapabilities", wifiCapabilities.isChecked());
                        intent.putExtra("wifiFrequency", wifiFrequency.isChecked());
                        intent.putExtra("wifiRSSI", wifiRSSI.isChecked());
                    }

                    // Fill in option for location
                    intent.putExtra("locationStatus", switchLocation.isChecked());
                    if(switchLocation.isChecked()){
                        intent.putExtra("locationGps", locGps.isChecked());
                        intent.putExtra("locationNet", locNetwork.isChecked());
                    }

                    // Fill in options for Global Database
                    intent.putExtra("globalDatabaseStatus", switchGdb.isChecked());

                    finish(); //Finish this activity and start new scan
                    startActivity(intent);
                }
            break;
        }
    }
    // Set Bluetooth Options' checkboxes OFF and color grey
    private void setBtOptionsOFF() {
        btDevName.setEnabled(false);
        btDevAddr.setEnabled(false);
        btDevType.setEnabled(false);
        btRSSI.setEnabled(false);
        btDevName.setChecked(false);
        btDevAddr.setChecked(false);
        btDevType.setChecked(false);
        btRSSI.setChecked(false);
        btDevName.setTextColor(getResources().getColor(R.color.material_grey_600));
        btDevAddr.setTextColor(getResources().getColor(R.color.material_grey_600));
        btDevType.setTextColor(getResources().getColor(R.color.material_grey_600));
        btRSSI.setTextColor(getResources().getColor(R.color.material_grey_600));
    }
    // Set Bluetooth Options' checkboxes enabled and color normal
    private void setBtOptionsON() {
        btDevName.setEnabled(true);
        btDevAddr.setEnabled(true);
        btDevType.setEnabled(true);
        btRSSI.setEnabled(true);
        btDevName.setChecked(true);
        btDevAddr.setChecked(true);
        btDevType.setChecked(true);
        btRSSI.setChecked(true);
        btDevName.setTextColor(getResources().getColor(R.color.mainColorCyan));
        btDevAddr.setTextColor(getResources().getColor(R.color.mainColorCyan));
        btDevType.setTextColor(getResources().getColor(R.color.mainColorCyan));
        btRSSI.setTextColor(getResources().getColor(R.color.mainColorCyan));
    }
    // Set Wifi Options' checkboxes enabled and color normal
    private void setWifiOptionsON() {
        wifiSSID.setEnabled(true);
        wifiBSSID.setEnabled(true);
        wifiCapabilities.setEnabled(true);
        wifiFrequency.setEnabled(true);
        wifiRSSI.setEnabled(true);
        wifiSSID.setChecked(true);
        wifiBSSID.setChecked(true);
        wifiCapabilities.setChecked(true);
        wifiFrequency.setChecked(true);
        wifiRSSI.setChecked(true);
        wifiSSID.setTextColor(getResources().getColor(R.color.mainColorCyan));
        wifiBSSID.setTextColor(getResources().getColor(R.color.mainColorCyan));
        wifiCapabilities.setTextColor(getResources().getColor(R.color.mainColorCyan));
        wifiFrequency.setTextColor(getResources().getColor(R.color.mainColorCyan));
        wifiRSSI.setTextColor(getResources().getColor(R.color.mainColorCyan));
    }
    // Set Wifi Options' checkboxes OFF and color grey
    private void setWifiOptionsOFF() {
        wifiSSID.setEnabled(false);
        wifiBSSID.setEnabled(false);
        wifiCapabilities.setEnabled(false);
        wifiFrequency.setEnabled(false);
        wifiRSSI.setEnabled(false);
        wifiSSID.setChecked(false);
        wifiBSSID.setChecked(false);
        wifiCapabilities.setChecked(false);
        wifiFrequency.setChecked(false);
        wifiRSSI.setChecked(false);
        wifiSSID.setTextColor(getResources().getColor(R.color.material_grey_600));
        wifiBSSID.setTextColor(getResources().getColor(R.color.material_grey_600));
        wifiCapabilities.setTextColor(getResources().getColor(R.color.material_grey_600));
        wifiFrequency.setTextColor(getResources().getColor(R.color.material_grey_600));
        wifiRSSI.setTextColor(getResources().getColor(R.color.material_grey_600));
    }

    // Set Location Options' checkboxes OFF and color grey
    private void setLocationOptionsOFF() {
        locGps.setEnabled(false);
        locNetwork.setEnabled(false);
        locGps.setChecked(false);
        locNetwork.setChecked(false);
        locGps.setTextColor(getResources().getColor(R.color.material_grey_600));
        locNetwork.setTextColor(getResources().getColor(R.color.material_grey_600));
    }

    private void setLocationOptionsON() {
        if(locationManager != null &&
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) &&
                !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            switchLocation.setChecked(false);
            Toast.makeText(this, "Unable to use GPS or network for location", Toast.LENGTH_SHORT).show();
        }

        if(locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locGps.setChecked(true);
            locGps.setEnabled(true);
            locGps.setTextColor(getResources().getColor(R.color.mainColorCyan));
        }
        if(locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locNetwork.setChecked(true);
            locNetwork.setEnabled(true);
            locNetwork.setTextColor(getResources().getColor(R.color.mainColorCyan));
        }
    }
}
