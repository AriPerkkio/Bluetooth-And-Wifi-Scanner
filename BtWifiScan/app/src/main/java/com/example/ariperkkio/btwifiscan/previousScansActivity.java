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
import android.widget.ListView;
import android.widget.TextView;

import java.sql.SQLException;

public class previousScansActivity extends Activity implements View.OnClickListener {

    private Button back;
    private ListView list;
    private prevScanCursorAdapter customCursorAdapter;
    private Cursor dataCursor;
    private databaseManager db = new databaseManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //Set full screen
        setContentView(R.layout.activity_previous_scans);

        back = (Button) findViewById(R.id.previousScansBack);
        back.setOnClickListener(this);

        list = (ListView) findViewById(R.id.previousscanlist);

        try{
            db.open();
            dataCursor = db.getScans();
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
                finish();
                break;
        }
    }

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
