package com.example.ariperkkio.btwifiscan;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Vector;

/**
 * Created by AriPerkkio on 01/05/16.
 */
public class GlobalDbConnection {
    private NetworkInfo networkInfo;
    private ConnectivityManager connMgr;
    private JSONArray jsonArray;
    private Context context;

    public GlobalDbConnection(Context ctx){
        context = ctx;
        connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();
        if( networkInfo != null && networkInfo.isConnected() ) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        else
            Log.e("GlobDbCon constr", "networkinfo: "+(networkInfo != null));
    }

    private class downloader extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls){
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid. \n" + e.toString();
            }
        }
        protected void onPostExecute(String result){
            Log.d("onPostExecute", result);
        }
    }

    public String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        int len = 2048;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            is = conn.getInputStream();
            return readIt(is, len);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private String readIt(InputStream stream, int len) throws IOException{
        Reader reader = new InputStreamReader(stream, "UTF-8");
        BufferedReader bfReader = new BufferedReader(reader, len);
        char[] buffer = new char[len];
        bfReader.read(buffer);
        return new String(buffer);
    }

    public List<scanResult> parseBtJson(String inputString) {
        List<scanResult> btDeviceList = new Vector<>();
        try {
            JSONObject  jsonRootObject = new JSONObject(inputString);
            jsonArray = jsonRootObject.optJSONArray("btscans");
            for(int i =0; i<jsonArray.length(); i++) {
                JSONObject newJsonObject = jsonArray.getJSONObject(i);
                btDeviceList.add(new scanResult(
                        (String) newJsonObject.get("devName"),
                        (String) newJsonObject.get("devAddr"),
                        reverseBtType((String) newJsonObject.get("devType")),
                        reverseRssi((String) newJsonObject.get("devRssi")),
                        (String) newJsonObject.get("location")));
            }
        }catch(JSONException e) {
            Log.e("JSON", e.toString());
        }
        return btDeviceList;
    }

    public List<scanResult> getAllBt(String server){
        String btJson = "";
        try {
            btJson = downloadUrl(server+"/getAllBt");
        }catch(java.io.IOException e) {
            Log.e("getAllBt", e.toString());
        }
        return parseBtJson(btJson);
    }

    public boolean pingServerOne(){
        String response = "";
        try {
            response = downloadUrl(context.getResources().getString(R.string.servOne)).split("\n")[0];
        }catch(java.io.IOException e) {
            Log.e("pingServerOne", e.toString());
        }
        return response.equals("Successfully read unknown request");
    }

    public boolean pingServerTwo(){
        String response = "";
        try {
            response = downloadUrl(context.getResources().getString(R.string.servTwo)).split("\n")[0];
        }catch(java.io.IOException e) {
            Log.e("pingServerTwo", e.toString());
        }
        return response.equals("Successfully read unknown request");
    }

    public boolean pingDbOne(String server){
        String response = "";
        try {
            response = downloadUrl(server+"/pingOkeanos").split("\n")[0];
        }catch(java.io.IOException e) {
            Log.e("pingDbOne", e.toString());
        }
        return response.equals("Okeanos-DB ping OK");
    }

    public boolean pingDbTwo(String server){
        String response = "";
        try {
            response = downloadUrl(server+"/pingAzure").split("\n")[0];
        }catch(java.io.IOException e) {
            Log.e("pingDbTwo", e.toString());
        }
        return response.equals("Azure-DB ping OK");
    }

    public boolean pingDbThree(String server){
        String response = "";
        try {
            response = downloadUrl(server+"/pingDigiOcean").split("\n")[0];
        }catch(java.io.IOException e) {
            Log.e("pingDbThree", e.toString());
        }
        return response.equals("DigitalOcean-DB ping OK");
    }

    private int reverseBtType(String type){
        switch(type) { // Convert int getType() into correct string
            case "Unknown":
                return 0;
            case "Classic":
                return 1;
            case "Low Energy":
                return 2;
            case "Dual Mode":
                return 3;
            default: // This is set when user unchecks Device Type from newScanActivity
                return 4;
        }
    }

    private int reverseRssi(String rssi){
        return Integer.parseInt(rssi.split("d")[0]); // I.e. "-113dbm" -> -113
    }
}
