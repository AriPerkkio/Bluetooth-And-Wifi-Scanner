package com.example.ariperkkio.btwifiscan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import java.sql.SQLException;

public class previousScansActivity extends Activity implements View.OnClickListener, ListView.OnItemClickListener, ListView.OnItemLongClickListener {

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
        list.setOnItemLongClickListener(this);

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

    // Listening long clicks on list items
    public boolean onItemLongClick(AdapterView<?> adapterView, View v, int position, long arg){
        final Cursor selectedObject = (Cursor) (list.getItemAtPosition(position)); //get clicked item
        String scanName = selectedObject.getString(1);
        String message = "Do you want to remove scan "+scanName+"?";
        AlertDialog.Builder builder = new AlertDialog.Builder(this); // Create new builder for alert dialog
        if(!scanName.equals("Global Database"))
            message = "Global Database cannot be removed";
        else
            builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() { // Add 'Remove' button for dialog - using anonymous click listener
                public void onClick(DialogInterface dialog, int id) {
                    try {
                        db.open();
                        db.deleteScan(selectedObject.getInt(0)); // Delete scan from db
                        dataCursor = db.getScans(); // Get all scans
                        db.close();
                        Toast.makeText(previousScansActivity.this, "Removed "+selectedObject.getString(1), Toast.LENGTH_SHORT).show();
                    } catch (SQLException e) {
                        Log.e("Delete:", e.toString());
                    }
                    customCursorAdapter.swapCursor(dataCursor); // Swap cursor to new one with updated values
                    customCursorAdapter.notifyDataSetChanged(); // Refresh list data
                }
            });
        // Add 'Cancel' button for dialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel(); // Hide dialog
            }
        });
        builder.setMessage(message).setTitle("Remove scan"); // Add message and title for dialog
        AlertDialog dialog = builder.create(); // Create dialog from previous attributes
        dialog.show(); // Show dialog
        return true;
    }

    // Cursor adapter for Scans
    private class prevScanCursorAdapter extends CursorAdapter {
        private LayoutInflater cursorInflater;

        public prevScanCursorAdapter (Context context, Cursor cursor, int flags) {
            super(context, cursor, flags);
            cursorInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void bindView(View view, Context context, Cursor cursor) {
            TextView scanName = (TextView) view.findViewById(R.id.prevscanrowName);
            TextView scanDate = (TextView) view.findViewById(R.id.prevscanrowDate);

            scanName.setText(cursor.getString(1));
            scanDate.setText(cursor.getString(2));
        }

        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return cursorInflater.inflate(R.layout.previousscanrow, parent, false);
        }
    }// Close inner class

}
