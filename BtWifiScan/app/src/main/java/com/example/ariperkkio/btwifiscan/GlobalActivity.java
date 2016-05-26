package com.example.ariperkkio.btwifiscan;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class GlobalActivity extends Activity implements View.OnClickListener, ListView.OnItemClickListener, HttpResponsePass{

    // UI Elements
    private Button btnGetAll;
    private Button btnMap;
    private Button btnSave;
    private Button btnSync;
    private Button btnBack;
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
    private int gdbId = 0;
    private databaseManager localDb;
    private saveResultsBackground localDbSaver;
    private Cursor btCursor;
    private Cursor wifiCursor;
    private boolean onBtSync = false; // Indicates if synchronizing has been started in the background
    private boolean onWifiSync = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //Set full screen
        setContentView(R.layout.activity_global);
        globalDbConnection = new GlobalDbConnection(this, this);
        localDb = new databaseManager(this);
        localDbSaver = new saveResultsBackground();
        btnGetAll = (Button) findViewById(R.id.globalBtnGetAll);
        btnMap = (Button) findViewById(R.id.globalBtnMap);
        btnSave = (Button) findViewById(R.id.globalBtnSave);
        btnSync = (Button) findViewById(R.id.globalBtnSync);
        btnBack = (Button) findViewById(R.id.globalBtnBack);
        btnGetAll.setOnClickListener(this);
        btnMap.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnSync.setOnClickListener(this);
        btnBack.setOnClickListener(this);
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
        mapsProgressDialog = new ProgressDialog(GlobalActivity.this);
        mapsProgressDialog.setMessage("Setting up maps... ");
        mapsProgressDialog.hide();
        globalDbConnection.pingServerOne();
        globalDbConnection.pingServerTwo();

        try {
            localDb.open();
            btCursor = localDb.getBtResultsById(gdbId);
            wifiCursor = localDb.getWifiResultsById(gdbId);
            localDb.close();
        }catch (SQLException e){
            Log.e("GlobalActivity cursors", e.toString());
        }
        for (int i = 0; i < btCursor.getCount(); i++){
            results.add(new scanResult(btCursor.getString(1),
                    btCursor.getString(2),
                    globalDbConnection.reverseBtType(btCursor.getString(3)),
                    Integer.parseInt(btCursor.getString(4)),
                    btCursor.getString(5)));
            btCursor.moveToNext();
        }
        for (int i = 0; i < wifiCursor.getCount(); i++) {
            results.add(new scanResult(wifiCursor.getString(1),
                    wifiCursor.getString(2),
                    wifiCursor.getString(3),
                    Integer.parseInt(wifiCursor.getString(4)),
                    Integer.parseInt(wifiCursor.getString(5)),
                    wifiCursor.getString(6)));
            wifiCursor.moveToNext();
        }
        listAdapter = new customAdapter(this, results);
        resultList.setAdapter(listAdapter);
    }

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
                compareDb();
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
            case "syncBt":
                onBtSync = false;
            case "syncWifi":
                if(method.equals("syncWifi")) onWifiSync = false;
                if(!onBtSync && !onWifiSync){ // Close progDiag when both syncs are complete
                    globalDbConnection.hideUploaderProgDiag();
                    Toast.makeText(this, "Synchronizing completed", Toast.LENGTH_SHORT).show();
                }
            case "getAllResults":
                for(int i=0;i<resultList.size();i++)
                    if(!macAddressList.contains(resultList.get(i).getMac())){
                        results.add(resultList.get(i));
                        macAddressList.add(resultList.get(i).getMac());
                    }
                listAdapter.notifyDataSetChanged();
            break;
        }
    }

    private void compareDb(){
        // TODO: If local copy is completely clear, 0 results, prompt to use .../getAllBt & .../getAllWifi instead of sync
        int btLocal= btCursor.getCount();
        int wifiLocal = wifiCursor.getCount();
        int btGlobal = Integer.parseInt(btCountText.getText().toString());
        int wifiGlobal = Integer.parseInt(wifiCountText.getText().toString());
        Log.d("compareDb", "Bt: "+btLocal+"/"+btGlobal+". Wifi: "+wifiLocal+"/"+wifiGlobal+".");
        String message = "";
        final boolean btSyncRequired = (btLocal!=btGlobal);
        final boolean wifiSyncRequired = (wifiLocal!=wifiGlobal);
        if(btSyncRequired)
            message = "Bluetooth results are not synchronized. ("+btLocal+"/"+btGlobal+")";
        if(wifiSyncRequired)
            message = "Wifi results are not synchronized. ("+wifiLocal+"/"+wifiGlobal+")";
        if(btSyncRequired && wifiSyncRequired)
            message = "Bluetooth and Wifi results are not synchronized. ("+btLocal+"/"+btGlobal+") & ("+wifiLocal+"/"+wifiGlobal+")";
        if(!message.equals("")){
            AlertDialog.Builder builder = new AlertDialog.Builder(this); // Create new builder for alert dialog
            builder.setMessage(message).setTitle("Synchronize results"); // Add message and title for dialog
            builder.setPositiveButton("Synchronize", new DialogInterface.OnClickListener() { // Add 'Remove' button for dialog - using anonymous click listener
                public void onClick(DialogInterface dialog, int id) {
                    if(btSyncRequired) {
                        onBtSync = true;
                        globalDbConnection.synchronizeBt(results, activeServer);
                    }
                    if(wifiSyncRequired){
                        onWifiSync = true;
                        globalDbConnection.synchronizeWifi(results, activeServer);
                    }

                    dialog.cancel();
                }
            });
            // Add 'Cancel' button for dialog
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel(); // Hide dialog
                }
            });
            AlertDialog dialog = builder.create(); // Create dialog from previous attributes
            dialog.show(); // Show dialog
        }

    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.globalBtnGetAll:
                results.clear();
                listAdapter.notifyDataSetChanged();
                globalDbConnection.getAllResults(activeServer);
            break;

            case R.id.globalBtnSave:
                try{
                    localDb.open();
                    localDb.deleteScan(gdbId);
                    localDb.createLocalGDB();
                    localDb.close();
                }catch(SQLException e){
                    Log.e("globalBtnGetAll", e.toString());
                }
                localDbSaver.execute(results);
            break;

            case R.id.globalBtnMap:
                mapsProgressDialog.show();
                mapsProgressDialog.setCanceledOnTouchOutside(false); // Force dialog show (disable click responsive)
                Intent intent = new Intent(getApplicationContext(), scanMap.class);
                intent.putExtra("Caller",this.getClass().toString());
                ResultListHolder resultListHolder = ResultListHolder.getInstance();
                resultListHolder.setResults(results);
                startActivity(intent);
            break;

            case R.id.globalBtnSync:
                onBtSync = true;
                globalDbConnection.synchronizeBt(results, activeServer);
                onWifiSync = true;
                globalDbConnection.synchronizeWifi(results, activeServer);
            break;

            case R.id.globalBtnBack:
                this.finish();
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

    // Background result saving with progress bar
    private class saveResultsBackground extends AsyncTask<List<scanResult>, Integer, Void> {
        ProgressDialog progressDialog = new ProgressDialog(GlobalActivity.this);

        // Before saving show progress dialog
        // Runs on UI thread - View updating allowed
        protected void onPreExecute() {
            progressDialog.setMessage("Saving results");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(false); //Set progress updates visible
            progressDialog.setMax(results.size());
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false); // Force dialog show (disable click responsive)
        }
        // After saving
        protected void onPostExecute(Void value) {
            // Refresh result history on mainActivity when finishing this one
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        // Save results and update progress dialog after each insert
        protected Void doInBackground(List<scanResult>... resultList) {
            try {
                localDb.open();
                int scanId = gdbId;

                for (int i = 0; i < resultList[0].size(); i++) {
                    if (resultList[0].get(i).getTechnology().equals("Bluetooth"))
                        localDb.insertIntoBtResults(scanId, resultList[0].get(i).getBtDevName(),
                                resultList[0].get(i).getBtDevAddr(), resultList[0].get(i).getBtDevType(),
                                resultList[0].get(i).getBtRSSI(), resultList[0].get(i).getLocation());
                    if (resultList[0].get(i).getTechnology().equals("Wifi")) {
                        localDb.insertIntoWifiResults(scanId, resultList[0].get(i).getWifiSSID(),
                                resultList[0].get(i).getWifiBSSID(), resultList[0].get(i).getWifiCapabilities(),
                                resultList[0].get(i).getWifiFrequency(), resultList[0].get(i).getWifiRSSI(), resultList[0].get(i).getLocation());
                    }
                    publishProgress(i); //Update current progress
                }
                localDb.close();
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
