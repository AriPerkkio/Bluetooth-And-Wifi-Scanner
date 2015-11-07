package com.example.ariperkkio.btwifiscan;

import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
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
    private Cursor btCursor;
    private Cursor wifiCursor;
    private Cursor bothCursors;


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

        list = (ListView) findViewById(R.id.subPrevScanList);

        try{
            database.open();
            // BtResults and WifiResults have different columns - both need own cursors
            btCursor = database.getBtResultsById(scanId);
            wifiCursor = database.getWifiResultsById(scanId);
            database.close();
        }catch (SQLException e) {
            Log.e("getBtResultsById: ", e.toString());
        }
        // MergeCursor combines both cursors into one
        bothCursors = new MergeCursor(new Cursor[] {btCursor, wifiCursor});
        listAdapter = new subPrevScanCursorAdapter(this, bothCursors, 0);
        list.setAdapter(listAdapter);
    }

    public void onClick(View v) {
        switch(v.getId()){
            case (R.id.subPrevScanBack):
                finish();
            break;

            case (R.id.subPrevScanRename):
                // ToDo: sql update to specified row with new name
                Toast.makeText(this, "rename", Toast.LENGTH_SHORT).show();
            break;
        }
    }

    // Custom Cursor adapter to handle results from database
    // Allows use of both wifi and bluetooth results - identified by columnCount
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

            // Bluetooth rows have 5 columns
            if (cursor.getColumnCount() == 5) {
                icon.setImageResource(R.drawable.bticon);
                fieldOne.setText(cursor.getString(1));
                fieldTwo.setText("");
                fieldThree.setText(cursor.getString(2));
                fieldFour.setText(cursor.getString(3));
                fieldFive.setText("Count: " + cursor.getColumnCount());
                if (cursor.getString(4).equals("0")) // btRSSI is 0 when user unchecks it from newScanActivity
                    fieldSix.setText("");
                else
                    fieldSix.setText("RSSI: " + cursor.getString(4) + " dBm");
            }

            // Wifi results have 6 rows
            if (cursor.getColumnCount()==6) {
                icon.setImageResource(R.drawable.wifiicon);
                fieldOne.setText(cursor.getString(1));
                fieldTwo.setText(cursor.getString(2));
                fieldThree.setText(cursor.getString(3));
                fieldFour.setText("");
                if(cursor.getString(4).equals("0")) // wifiFrequency is 0 when user unchecks it from newScanActivity
                    fieldFive.setText("");
                else
                    fieldFive.setText(cursor.getString(4) + " MHz");
                if(cursor.getString(5).equals("0")) // wifiRSSI is 0 when user unchecks it from newScanActivity
                    fieldSix.setText("");
                else
                    fieldSix.setText("RSSI: "+cursor.getString(5)+" dbm"); // crashes when only int as text
            }
        }

        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return cursorInflater.inflate(R.layout.scanrow, parent, false);
        }
    }// Close inner class
}
