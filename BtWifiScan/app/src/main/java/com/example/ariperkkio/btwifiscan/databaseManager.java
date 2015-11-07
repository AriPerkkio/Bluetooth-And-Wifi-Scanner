package com.example.ariperkkio.btwifiscan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.sql.SQLException;

/**
 * Created by arska on 05/11/15.
 */

// Tables:
// Scans: idScans, Name, Time
// BluetoothResults: idScans, DeviceName, DeviceAddress, DeviceType, RSSI
// WifiResults: idScans, SSID, BSSID, Capabilities, RSSI, Frequency
public class databaseManager {
    // Table Scans and its columns
    private static final String DATABASE_TABLE_SCANS = "Scans";
    public static final String scans_ScanId = "_id";
    public static final String scans_ScanName = "ScanName";
    public static final String scans_Time = "Time";

    // Table BluetoothResulta and its columns
    private static final String DATABASE_TABLE_BTRESULTS = "BluetoothResults";
    public static final String btResults_ScanId = "_id";
    public static final String btResults_DevName = "DeviceName";
    public static final String btResults_DevAddr = "DeviceAddress";
    public static final String btResults_DevType = "DeviceType";
    public static final String btResults_RSSI = "RSSI";

    // Table WifiResults and its rows
    private static final String DATABASE_TABLE_WIFIRESULTS = "WifiResults";
    public static final String wifiResults_ScanId = "_id";
    public static final String wifiResults_SSID = "SSID";
    public static final String wifiResults_BSSID = "BSSID";
    public static final String wifiResults_Capabilities = "Capabilities";
    public static final String wifiResults_RSSI = "RSSI";
    public static final String wifiResults_Frequency = "Frequency";

    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE_SCANS =
            "CREATE TABLE Scans(_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                                "ScanName TEXT, "+
                                "Time DATETIME DEFAULT CURRENT_DATE)";
    private static final String CREATE_TABLE_BTRESULTS =
            "CREATE TABLE BluetoothResults(_id integer, "+
                                            "DeviceName TEXT, "+
                                            "DeviceAddress TEXT, "+
                                            "DeviceType TEXT, "+
                                            "RSSI INTEGER, " +
                                            "FOREIGN KEY (_id) REFERENCES Scans(_id))";
    private static final String CREATE_TABLE_WIFIRESULTS =
            "CREATE TABLE WifiResults(_id integer, "+
                                        "SSID TEXT, "+
                                        "BSSID TEXT, "+
                                        "Capabilities TEXT, "+
                                        "RSSI INTEGER, " +
                                        "Frequency INTEGER, "+
                                        "FOREIGN KEY (_id) REFERENCES Scans(_id))";

    private final Context context;
    private MyDatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public databaseManager(Context ctx) {
        this.context = ctx;
        DBHelper = new MyDatabaseHelper(context);
    }

    private static class MyDatabaseHelper extends SQLiteOpenHelper {

        MyDatabaseHelper(Context context) {
            super(context, "Scans", null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            //exec database statement
            db.execSQL(CREATE_TABLE_SCANS);
            db.execSQL(CREATE_TABLE_BTRESULTS);
            db.execSQL(CREATE_TABLE_WIFIRESULTS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE Scans;");
            db.execSQL("DROP TABLE BluetoothResults;");
            db.execSQL("DROP TABLE WifiResults;");
            db.execSQL(CREATE_TABLE_SCANS);
            db.execSQL(CREATE_TABLE_BTRESULTS);
            db.execSQL(CREATE_TABLE_WIFIRESULTS);
        }
    } //End inner class

    public databaseManager open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        DBHelper.close();
    }

    public void startOver(){
        db.execSQL("DROP TABLE Scans;");
        db.execSQL("DROP TABLE BluetoothResults;");
        db.execSQL("DROP TABLE WifiResults;");
    }

    public long insertIntoScans(String scanName) {
        // ScanId is Autoincremented, Time is CURRENT_DATE by default
        ContentValues initialValues = new ContentValues();
        initialValues.put(scans_ScanName, scanName);
        return db.insert(DATABASE_TABLE_SCANS, null, initialValues);
    }

    public long insertIntoBtResults(int idScans, String DeviceName, String DeviceAddres, String DeviceType, int RSSI){
        ContentValues initialValues = new ContentValues();
        initialValues.put(btResults_ScanId, idScans);
        initialValues.put(btResults_DevName, DeviceName);
        initialValues.put(btResults_DevAddr, DeviceAddres);
        initialValues.put(btResults_DevType, DeviceType);
        initialValues.put(btResults_RSSI, RSSI);
        return db.insert(DATABASE_TABLE_BTRESULTS, null, initialValues);
    }

    public long insertIntoWifiResults(int idScans, String SSID, String BSSID, String Capabilities, int Frequency, int RSSI){
        ContentValues initialValues = new ContentValues();
        initialValues.put(wifiResults_ScanId, idScans);
        initialValues.put(wifiResults_SSID, SSID);
        initialValues.put(wifiResults_BSSID, BSSID);
        initialValues.put(wifiResults_Capabilities, Capabilities);
        initialValues.put(wifiResults_Frequency, Frequency);
        initialValues.put(wifiResults_RSSI, RSSI);
        return db.insert(DATABASE_TABLE_WIFIRESULTS, null, initialValues);
    }

   public Cursor getScans() throws SQLException {
        Cursor mCursor = db.query(true, DATABASE_TABLE_SCANS, new String[] {
                scans_ScanId, scans_ScanName, scans_Time}, null, null, null, null, null, null);
        if(mCursor!=null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public int getHighestId() {
        final SQLiteStatement statement = db.compileStatement("SELECT MAX(_id) FROM Scans");
        return (int) statement.simpleQueryForLong();
    }

    public Cursor getBtResultsById(int rowId) throws SQLException {
        Cursor mCursor = db.query(true, DATABASE_TABLE_BTRESULTS, new String[] {
                btResults_ScanId, btResults_DevName, btResults_DevAddr, btResults_DevType, btResults_RSSI}, btResults_ScanId + "=" + rowId, null, null, null, null, null);
        if(mCursor!=null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

}