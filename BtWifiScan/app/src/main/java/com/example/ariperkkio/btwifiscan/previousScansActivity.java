package com.example.ariperkkio.btwifiscan;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.SQLException;

public class previousScansActivity extends Activity implements View.OnClickListener, ListView.OnItemClickListener {

    private Button back;
    private ListView list;
    private prevScanCursorAdapter customCursorAdapter;
    private Cursor dataCursor;
    private databaseManager db = new databaseManager(this);
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //Set full screen
        setContentView(R.layout.activity_previous_scans);

        back = (Button) findViewById(R.id.previousScansBack);
        back.setOnClickListener(this);

        list = (ListView) findViewById(R.id.previousscanlist);
        list.setOnItemClickListener(this);

        try{
            db.open();
            dataCursor = db.getScans(); // Gets all scans from database to cursor
            db.close();
        }catch (SQLException e) {
            Log.e("db.getScans() : ", e.toString());
        }

        customCursorAdapter = new prevScanCursorAdapter(this, dataCursor, 0);
        list.setAdapter(customCursorAdapter);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.previousScansBack):
                // If there has been renaming of a scan, it means there is another previousScanBack activity with old scan name
                // in background. Solution: Close all activities when clicking back, open mainActivity.
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            break;
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View v, int position, long arg){
        Cursor selectedObject = (Cursor) (list.getItemAtPosition(position));
        intent = new Intent(previousScansActivity.this, subPrevScanActivity.class); //Create intent for subPrevScanActivity
        // Fill in details of selected scan
        intent.putExtra("scanId", selectedObject.getInt(0));
        intent.putExtra("scanName", selectedObject.getString(1));
        intent.putExtra("scanDate", selectedObject.getString(2));
        startActivity(intent); // Start subPrevScanActivity but keep this one alive
    }

    // Cursor adapter for Scans
    private class prevScanCursorAdapter extends CursorAdapter {
        private LayoutInflater cursorInflater;

        public prevScanCursorAdapter (Context context, Cursor cursor, int flags) {
            super(context, cursor, flags);
            cursorInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void bindView(View view, Context context, Cursor cursor) {
            TextView scanId = (TextView) view.findViewById(R.id.prevscanrowId);
            TextView scanName = (TextView) view.findViewById(R.id.prevscanrowName);
            TextView scanDate = (TextView) view.findViewById(R.id.prevscanrowDate);

            scanId.setText(cursor.getString(0));
            scanName.setText(cursor.getString(1));
            scanDate.setText(cursor.getString(2));
        }

        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return cursorInflater.inflate(R.layout.previousscanrow, parent, false);
        }
    }// Close inner class

}
