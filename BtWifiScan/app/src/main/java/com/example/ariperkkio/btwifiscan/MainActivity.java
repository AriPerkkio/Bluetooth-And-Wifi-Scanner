package com.example.ariperkkio.btwifiscan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;

public class MainActivity extends Activity implements View.OnClickListener {

    private Button buttonNew;
    private Button buttonHistory;
    private Intent intent;
    private databaseManager database;
    private TextView scanNumber;
    private TextView btNumber;
    private TextView wifiNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //Set full screen
        setContentView(R.layout.activity_main);

        buttonNew = (Button) findViewById(R.id.mainBtnNewScan);
        buttonHistory = (Button) findViewById(R.id.mainBtnHistory);
        buttonNew.setOnClickListener(this);
        buttonHistory.setOnClickListener(this);
        scanNumber = (TextView) findViewById(R.id.mainScansNumber);
        btNumber = (TextView) findViewById(R.id.mainBtNumber);
        wifiNumber = (TextView) findViewById(R.id.mainWifiNumber);
        database = new databaseManager(this);

        // Get latest values for result history text fields
        try{
            database.open();
            scanNumber.setText("" + database.getNumberOfScans());
            wifiNumber.setText(""+database.getNumberOfWifi());
            btNumber.setText(""+database.getNumberOfBt());
            database.close();
        } catch (SQLException e) {
            Log.e("DB: ", e.toString());
        }

    }

    public void onClick (View v) {
        switch(v.getId()) {
            case (R.id.mainBtnNewScan): // 'New scan' button
                intent = new Intent(MainActivity.this, newScanActivity.class); //Create intent for newScanActivity
                startActivity(intent);
            break;

            case (R.id.mainBtnHistory): // 'View History' button
                intent = new Intent(MainActivity.this, previousScansActivity.class); //Create intent for newScanActivity
                startActivity(intent);
            break;
        }
    }
}
