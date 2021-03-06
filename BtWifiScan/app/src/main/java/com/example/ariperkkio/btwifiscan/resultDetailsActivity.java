package com.example.ariperkkio.btwifiscan;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// This Activity can be accessed from subPrevScanActivity and scanActivity
// Both are passing same extras, identified by 'technology' field
public class resultDetailsActivity extends Activity implements View.OnClickListener{

    Button back;
    TextView mainOne;
    TextView fieldOne;
    TextView mainTwo;
    TextView fieldTwo;
    TextView mainThree;
    TextView fieldThree;
    TextView mainFour;
    TextView fieldFour;
    TextView mainFive;
    TextView fieldFive;
    TextView mainSix;
    TextView fieldSix;
    LinearLayout layoutSix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //Set full screen
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); //Hide automatic keyboard
        setContentView(R.layout.activity_result_details);

        back = (Button) findViewById(R.id.resultDetailsBack);
        back.setOnClickListener(this);

        // See XML for clarification about IDs
        mainOne = (TextView) findViewById(R.id.resultDetailsMainOne);
        fieldOne = (TextView) findViewById(R.id.resultDetailsOne);
        mainTwo = (TextView) findViewById(R.id.resultDetailsMainTwo);
        fieldTwo = (TextView) findViewById(R.id.resultDetailsTwo);
        mainThree = (TextView) findViewById(R.id.resultDetailsMainThree);
        fieldThree = (TextView) findViewById(R.id.resultDetailsThree);
        mainFour = (TextView) findViewById(R.id.resultDetailsMainFour);
        fieldFour = (TextView) findViewById(R.id.resultDetailsFour);
        mainFive = (TextView) findViewById(R.id.resultDetailsMainFive);
        fieldFive = (TextView) findViewById(R.id.resultDetailsFive);
        mainSix = (TextView) findViewById(R.id.resultDetailsMainSix);
        fieldSix = (TextView) findViewById(R.id.resultDetailsSix);
        layoutSix = (LinearLayout) findViewById(R.id.linearLayout6);

        // If attribute is null or empty, set it as <attribute not scanned>. Otherwise set requested attribute as value.
        // NOTE: Checking for null must be first, otherwise null pointer exception may happen
        if(getIntent().getExtras().getString("technology") != null && getIntent().getExtras().getString("technology").equals("Bluetooth")){
            mainOne.setText("Device name");
            if(getIntent().getExtras().getString("btDevName") == null || getIntent().getExtras().getString("btDevName").equals(""))
                fieldOne.setText("<attribute not scanned>");
            else
                fieldOne.setText(getIntent().getExtras().getString("btDevName"));
            mainTwo.setText("Device Address");
            if(getIntent().getExtras().getString("btDevAddr") == null || getIntent().getExtras().getString("btDevAddr").equals(""))
                fieldTwo.setText("<attribute not scanned>");
            else
                fieldTwo.setText(getIntent().getExtras().getString("btDevAddr"));
            mainThree.setText("Device type");
            if(getIntent().getExtras().getString("btDevType") == null ||getIntent().getExtras().getString("btDevType").equals(""))
                fieldThree.setText("<attribute not scanned>");
            else
                fieldThree.setText(getIntent().getExtras().getString("btDevType"));
            mainFour.setText("RSSI");
            if(getIntent().getExtras().getString("btRSSI") == null ||getIntent().getExtras().getString("btRSSI").equals("0"))
                fieldFour.setText("<attribute not scanned>");
            else
                fieldFour.setText(getIntent().getExtras().getString("btRSSI")+"dBm");
            mainFive.setText("Location");
            if(getIntent().getExtras().getString("location") == null || getIntent().getExtras().getString("location").equals(""))
                fieldFive.setText("<Location not scanned>");
            else
            {
                String[] location = getIntent().getExtras().getString("location").split(",");
                double latitude = Double.parseDouble(location[0]);
                double longitude = Double.parseDouble(location[1]);
                fieldFive.setText("Latitude: " +latitude+ "\nLongitude: " +longitude);
            }
            layoutSix.setVisibility(View.GONE);
        }

        if(getIntent().getExtras().getString("technology") != null && getIntent().getExtras().getString("technology").equals("Wifi")){
            mainOne.setText("SSID");
            if(getIntent().getExtras().getString("wifiSSID") == null || getIntent().getExtras().getString("wifiSSID").equals(""))
                fieldOne.setText("<attribute not scanned>");
            else
                fieldOne.setText(getIntent().getExtras().getString("wifiSSID"));
            mainTwo.setText("BSSID");
            if(getIntent().getExtras().getString("wifiBSSID") == null || getIntent().getExtras().getString("wifiBSSID").equals(""))
                fieldTwo.setText("<attribute not scanned>");
            else
                fieldTwo.setText(getIntent().getExtras().getString("wifiBSSID"));
            mainThree.setText("Capabilities");
            if(getIntent().getExtras().getString("wifiCapabilities") == null || getIntent().getExtras().getString("wifiCapabilities").equals(""))
                fieldThree.setText("<attribute not scanned>");
            else
                fieldThree.setText(getIntent().getExtras().getString("wifiCapabilities"));
            mainFour.setText("Frequency");
            if(getIntent().getExtras().getString("wifiFrequency") == null || getIntent().getExtras().getString("wifiFrequency").equals("0"))
                fieldFour.setText("<attribute not scanned>");
            else
                fieldFour.setText(getIntent().getExtras().getString("wifiFrequency")+"MHz");
            mainFive.setText("RSSI");
            if(getIntent().getExtras().getString("wifiRSSI") == null || getIntent().getExtras().getString("wifiRSSI").equals("0"))
                fieldFive.setText("<attribute not scanned>");
            else
                fieldFive.setText(getIntent().getExtras().getString("wifiRSSI") +"dBm");
            mainSix.setText("Location");
            if(getIntent().getExtras().getString("location") == null || getIntent().getExtras().getString("location").equals(""))
                fieldSix.setText("<Location not scanned>");
            else {
                String[] location = getIntent().getExtras().getString("location").split(",");
                double latitude = Double.parseDouble(location[0]);
                double longitude = Double.parseDouble(location[1]);
                fieldSix.setText("Latitude: " + latitude + "\nLongitude: " + longitude);
            }
        }
    }

    public void onClick(View v){
        switch(v.getId()){
            case (R.id.resultDetailsBack):
                finish();
            break;
        }
    }
}
