package com.example.ariperkkio.btwifiscan;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.List;

public class subPrevScanActivity extends Activity implements View.OnClickListener{

    private Button back;
    private Button rename;
    private EditText scanNameField;
    private TextView scanDate;
    private TextView btNumber;
    private TextView wifiNumber;
    private int scanId;
    private ListView list;
    private databaseManager database;
    private subPrevScanCursorAdapter listAdapter;
    private Cursor dataCursor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //Set full screen
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); //Hide automatic keyboard
        setContentView(R.layout.activity_sub_prev_scan);

        back = (Button) findViewById(R.id.subPrevScanBack);
        back.setOnClickListener(this);
        rename = (Button) findViewById(R.id.subPrevScanRename);
        rename.setOnClickListener(this);
        scanNameField = (EditText) findViewById(R.id.subPrevScanName);
        scanDate = (TextView) findViewById(R.id.subPrevScanDate);
        btNumber = (TextView) findViewById(R.id.subPrevScanBtNumber);
        wifiNumber = (TextView) findViewById(R.id.subPrevScanWifiNumber);
        database = new databaseManager(this);

        scanNameField.setText(getIntent().getExtras().getString("scanName"));
        scanDate.setText(getIntent().getExtras().getString("scanDate"));
        scanId = getIntent().getExtras().getInt("scanId");

        try{
            database.open();
            dataCursor = database.getBtResultsById(scanId);
            database.close();
        }catch (SQLException e) {
            Log.e("getBtResultsById: ", e.toString());
        }

        list = (ListView) findViewById(R.id.subPrevScanList);

        listAdapter = new subPrevScanCursorAdapter(this, dataCursor, 0);
        list.setAdapter(listAdapter);

    }

    public void onClick(View v) {
        switch(v.getId()){
            case (R.id.subPrevScanBack):
                finish();
            break;

            case (R.id.subPrevScanRename):
                Toast.makeText(this, "rename", Toast.LENGTH_SHORT).show();
            break;
        }
    }

    private class subPrevScanCursorAdapter extends CursorAdapter {

        private LayoutInflater cursorInflater;

        public subPrevScanCursorAdapter (Context context, Cursor cursor, int flags) {
            super(context, cursor, flags);
            cursorInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void bindView(View view, Context context, Cursor cursor) {

            ImageView icon = (ImageView) view.findViewById(R.id.scanrowIcon);
            TextView fieldOne = (TextView) view.findViewById(R.id.scanrowOne);
            TextView fieldTwo = (TextView) view.findViewById(R.id.scanrowTwo);
            TextView fieldThree = (TextView) view.findViewById(R.id.scanrowThree);
            TextView fieldFour = (TextView) view.findViewById(R.id.scanrowFour);
            TextView fieldFive = (TextView) view.findViewById(R.id.scanrowFive);
            TextView fieldSix = (TextView) view.findViewById(R.id.scanrowSix);

                icon.setImageResource(R.drawable.bticon);
                fieldOne.setText(cursor.getString(1));
                fieldTwo.setText("");
                fieldThree.setText(cursor.getString(2));
                fieldFour.setText(cursor.getString(3));
                fieldFive.setText("");
                if(cursor.getString(4).equals("0")) // btRSSI is 0 when user unchecks it from newScanActivity
                    fieldSix.setText("");
                else
                    fieldSix.setText("RSSI: " + cursor.getString(4)+" dBm");
            }

        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return cursorInflater.inflate(R.layout.scanrow, parent, false);
        }
    }// Close inner class
}
