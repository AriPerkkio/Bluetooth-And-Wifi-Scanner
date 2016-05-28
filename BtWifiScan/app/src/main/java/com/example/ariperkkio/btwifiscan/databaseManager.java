package com.example.ariperkkio.btwifiscan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.sql.SQLException;

/**
 * Created by arska on 05/11/15.
 */

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
    public static final String btResults_Location = "Location";

    // Table WifiResults and its rows
    private static final String DATABASE_TABLE_WIFIRESULTS = "WifiResults";
    public static final String wifiResults_ScanId = "_id";
    public static final String wifiResults_SSID = "SSID";
    public static final String wifiResults_BSSID = "BSSID";
    public static final String wifiResults_Capabilities = "Capabilities";
    public static final String wifiResults_RSSI = "RSSI";
    public static final String wifiResults_Frequency = "Frequency";
    public static final String wifiResults_Location = "Location";

    // Local copy of Global Database
    public static final int globaldb_id = 0;

    private static final int DATABASE_VERSION = 4;

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
                                            "Location TEXT, "+
                                            "FOREIGN KEY (_id) REFERENCES Scans(_id))";
    private static final String CREATE_TABLE_WIFIRESULTS =
            "CREATE TABLE WifiResults(_id integer, "+
                                        "SSID TEXT, "+
                                        "BSSID TEXT, "+
                                        "Capabilities TEXT, "+
                                        "RSSI INTEGER, " +
                                        "Frequency INTEGER, "+
                                        "Location TEXT, "+
                                        "FOREIGN KEY (_id) REFERENCES Scans(_id))";
    private static final String CREATE_ROW_GLOBALDB =
            "INSERT INTO Scans(_id, ScanName) VALUES("+globaldb_id+", \"Global Database\")";

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
            db.execSQL(CREATE_ROW_GLOBALDB);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE Scans;");
            db.execSQL("DROP TABLE BluetoothResults;");
            db.execSQL("DROP TABLE WifiResults;");
            db.execSQL(CREATE_TABLE_SCANS);
            db.execSQL(CREATE_TABLE_BTRESULTS);
            db.execSQL(CREATE_TABLE_WIFIRESULTS);
            db.execSQL(CREATE_ROW_GLOBALDB);
        }
    } //End inner class

    public databaseManager open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        DBHelper.close();
    }

    public long insertIntoScans(String scanName) {
        // ScanId is Auto incremented, Time is CURRENT_DATE by default
        ContentValues initialValues = new ContentValues();
        initialValues.put(scans_ScanName, scanName);
        return db.insert(DATABASE_TABLE_SCANS, null, initialValues);
    }

    public long createLocalGDB(){
        // Create ID for global DB results
        ContentValues initialValues = new ContentValues();
        initialValues.put(scans_ScanName, "Global Database");
        initialValues.put(scans_ScanId, globaldb_id);
        return db.insert(DATABASE_TABLE_SCANS, null, initialValues);
    }

    public long insertIntoBtResults(int idScans, String DeviceName, String DeviceAddress, String DeviceType, int RSSI, String Location){
        ContentValues initialValues = new ContentValues();
        initialValues.put(btResults_ScanId, idScans);
        initialValues.put(btResults_DevName, DeviceName);
        initialValues.put(btResults_DevAddr, DeviceAddress);
        initialValues.put(btResults_DevType, DeviceType);
        initialValues.put(btResults_RSSI, RSSI);
        initialValues.put(btResults_Location, Location);
        return db.insert(DATABASE_TABLE_BTRESULTS, null, initialValues);
    }

    public long insertIntoWifiResults(int idScans, String SSID, String BSSID, String Capabilities, int Frequency, int RSSI, String Location){
        ContentValues initialValues = new ContentValues();
        initialValues.put(wifiResults_ScanId, idScans);
        initialValues.put(wifiResults_SSID, SSID);
        initialValues.put(wifiResults_BSSID, BSSID);
        initialValues.put(wifiResults_Capabilities, Capabilities);
        initialValues.put(wifiResults_Frequency, Frequency);
        initialValues.put(wifiResults_RSSI, RSSI);
        initialValues.put(wifiResults_Location, Location);
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

    public int getNumberOfScans() {
        final SQLiteStatement statement = db.compileStatement("SELECT count(*) FROM Scans");
        return (int) statement.simpleQueryForLong();
    }

    public int getNumberOfBt() {
        final SQLiteStatement statement = db.compileStatement("SELECT count(*) FROM BluetoothResults");
        return (int) statement.simpleQueryForLong();
    }

    public int getNumberOfWifi() {
        final SQLiteStatement statement = db.compileStatement("SELECT count(*) FROM WifiResults");
        return (int) statement.simpleQueryForLong();
    }

    public Cursor getBtResultsById(int rowId) throws SQLException {
        Cursor mCursor = db.query(true, DATABASE_TABLE_BTRESULTS, new String[]{
                btResults_ScanId, btResults_DevName, btResults_DevAddr, btResults_DevType, btResults_RSSI, btResults_Location}, btResults_ScanId + "=" + rowId, null, null, null, null, null);
        if(mCursor!=null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor getWifiResultsById(int rowId) throws SQLException {
        Cursor mCursor = db.query(true, DATABASE_TABLE_WIFIRESULTS, new String[] {
                wifiResults_ScanId, wifiResults_SSID, wifiResults_BSSID, wifiResults_Capabilities, wifiResults_Frequency, wifiResults_RSSI, wifiResults_Location}, wifiResults_ScanId + "=" + rowId, null, null, null, null, null);
        if(mCursor!=null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public int getNumberOfBtById(int rowId) throws SQLException  {
        final SQLiteStatement statement = db.compileStatement("SELECT COUNT(*) FROM BluetoothResults WHERE _id = " + rowId);
        return (int) statement.simpleQueryForLong();
    }

    public int getNumberOfWifiById(int rowId) throws SQLException {
        final SQLiteStatement statement = db.compileStatement("SELECT COUNT(*) FROM WifiResults WHERE _id = "+rowId);
        return (int) statement.simpleQueryForLong();
    }

    // String variable is from text input
    public void renameScan(int rowId, String newName) throws SQLException {
        ContentValues values = new ContentValues();
        values.put(scans_ScanName, newName);
        db.update(DATABASE_TABLE_SCANS, values, "_id="+rowId, null);
    }

    // Delete scan and its results
    public void deleteScan(int scanId) throws SQLException{
        db.delete(DATABASE_TABLE_BTRESULTS, "_id=" + scanId, null);
        db.delete(DATABASE_TABLE_WIFIRESULTS, "_id=" + scanId, null);
        db.delete(DATABASE_TABLE_SCANS,"_id="+scanId,null);
    }

    // TODO: Add location as parameter when deleting result
    public void deleteResult(int scanId, String technology, String address){
        if(technology.equals("Bluetooth"))
            db.delete(DATABASE_TABLE_BTRESULTS,"_id="+scanId+" AND DeviceAddress='"+address+"'" ,null);
        if(technology.equals("Wifi"))
            db.delete(DATABASE_TABLE_WIFIRESULTS,"_id="+scanId+" AND BSSID='"+address+"'" ,null);
    }
}