package com.example.ariperkkio.btwifiscan;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class GlobalActivity extends Activity implements View.OnClickListener, ListView.OnItemClickListener, HttpResponsePass{

    // UI Elements
    private Button btnGetAll;
    private Button btnMap;
    private ListView resultList;
    private TextView servOneText;
    private TextView servTwoText;
    private String activeServer;
    private TextView dbOneText;
    private TextView dbTwoText;
    private TextView dbThreeText;
    private TextView btCountText;
    private TextView wifiCountText;
    public static ProgressDialog mapsProgressDialog;

    private customAdapter listAdapter;
    private ArrayList<scanResult> results;
    private ArrayList<String> macAddressList;
    private GlobalDbConnection globalDbConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //Set full screen
        setContentView(R.layout.activity_global);
        globalDbConnection = new GlobalDbConnection(this, this);
        btnGetAll = (Button) findViewById(R.id.globalBtnGetAll);
        btnMap = (Button) findViewById(R.id.globalBtnMap);
        btnGetAll.setOnClickListener(this);
        btnMap.setOnClickListener(this);
        servOneText = (TextView) findViewById(R.id.globalServOne);
        servTwoText = (TextView) findViewById(R.id.globalServTwo);
        dbOneText = (TextView) findViewById(R.id.globalDbOne);
        dbTwoText = (TextView) findViewById(R.id.globalDbTwo);
        dbThreeText = (TextView) findViewById(R.id.globalDbThree);
        btCountText = (TextView) findViewById(R.id.globalBtCount);
        wifiCountText = (TextView) findViewById(R.id.globalWifiCount);
        results = new ArrayList<scanResult>();
        macAddressList = new ArrayList<String>();
        resultList = (ListView) findViewById(R.id.globalListResults);
        resultList.setOnItemClickListener(this);
        listAdapter = new customAdapter(this, results);
        resultList.setAdapter(listAdapter);
        mapsProgressDialog = new ProgressDialog(GlobalActivity.this);
        mapsProgressDialog.setMessage("Setting up maps... ");
        mapsProgressDialog.hide();
        globalDbConnection.pingServerOne();
        globalDbConnection.pingServerTwo();
    }

    public void onResponseRead(String response){ };
    public void onResponseRead(String response, String method){
        switch(method){
            case "pingServerOne":
                if(!response.equals("Successfully read unknown request")) break;
                servOneText.setText(R.string.servOneOnline);
                servOneText.setTextColor(getResources().getColor(R.color.ONLINE));
                setActiveServer(getString(R.string.servOne));
            break;
            case "pingServerTwo":
                if(!response.equals("Successfully read unknown request")) break;
                servTwoText.setText(R.string.servTwoOnline);
                servTwoText.setTextColor(getResources().getColor(R.color.ONLINE));
                if (activeServer == null) // Server one offline
                    setActiveServer(getString(R.string.servTwo));
            break;
            case "pingDbOne":
                if(!response.equals("Okeanos-DB ping OK")) break;
                dbOneText.setText(R.string.dbOneOnline);
                dbOneText.setTextColor(getResources().getColor(R.color.ONLINE));
            break;
            case "pingDbTwo":
                if(!response.equals("DigitalOcean-DB ping OK")) break;
                dbTwoText.setText(R.string.dbTwoOnline);
                dbTwoText.setTextColor(getResources().getColor(R.color.ONLINE));
            break;
            case "pingDbThree":
                if(!response.equals("Azure-DB ping OK")) break;
                dbThreeText.setText(R.string.dbThreeOnline);
                dbThreeText.setTextColor(getResources().getColor(R.color.ONLINE));
            break;
            case "countBt":
                btCountText.setText(response.split("Devices: ")[1]); // I.e. "Count of Bluetooth Devices: 12" -> 12
                btCountText.setTextColor(getResources().getColor(R.color.ONLINE));
            break;
            case "countWifi":
                wifiCountText.setText(response.split("Networks: ")[1]); // I.e. "Count of Wifi Networks: 9" -> 9
                wifiCountText.setTextColor(getResources().getColor(R.color.ONLINE));
            break;
        }
    }

    private void setActiveServer(String _server){
        activeServer = _server;
        globalDbConnection.pingDbOne(activeServer);
        globalDbConnection.pingDbTwo(activeServer);
        globalDbConnection.pingDbThree(activeServer);
        globalDbConnection.getBtCount(activeServer);
        globalDbConnection.getWifiCount(activeServer);
    }

    public void scanResultPass(String method, List<scanResult> resultList){
        switch(method){
            case "getAllResults":
                Log.i("GlobalActivity", "Received "+resultList.size());
                for(int i=0;i<resultList.size();i++)
                    if(!macAddressList.contains(resultList.get(i).getMac())){
                        results.add(resultList.get(i));
                        macAddressList.add(resultList.get(i).getMac());
                    }
                listAdapter.notifyDataSetChanged();
            break;
        }
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.globalBtnGetAll:
                globalDbConnection.getAllResults(activeServer);
            break;

            case R.id.globalBtnMap:
                mapsProgressDialog.show();
                mapsProgressDialog.setCanceledOnTouchOutside(false); // Force dialog show (disable click responsive)
                Intent intent = new Intent(getApplicationContext(), scanMap.class);
                intent.putExtra("Caller",this.getClass().toString());
                int wifiCounter = 0;
                int btCounter = 0;

                for (int i = 0; i < results.size(); i++){
                    if(results.get(i).getTechnology().equals("Wifi")){
                        intent.putExtra("wifi " + wifiCounter,
                                results.get(i).getWifiSSID() + "\n" +
                                        results.get(i).getWifiBSSID() + "\n" +
                                        results.get(i).getWifiCapabilities() + "\n" +
                                        results.get(i).getWifiFrequency() + "\n" +
                                        results.get(i).getWifiRSSI() + "\n" +
                                        results.get(i).getLocation());
                        wifiCounter++;
                    }
                    else{
                        intent.putExtra("bluetooth " + btCounter,
                                results.get(i).getBtDevName() + "\n" +
                                        results.get(i).getBtDevAddr() + "\n" +
                                        results.get(i).getBtDevType() + "\n" +
                                        results.get(i).getBtRSSI() + "\n" +
                                        results.get(i).getLocation());
                        btCounter++;
                    }
                }
                intent.putExtra("BtCount", btCounter);
                intent.putExtra("WifiCount", wifiCounter);
                startActivity(intent);

            break;
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View v, int position, long arg) {
        scanResult selectedObject = (scanResult) (resultList.getItemAtPosition(position));
        Intent intent = new Intent(GlobalActivity.this, resultDetailsActivity.class);

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
}
