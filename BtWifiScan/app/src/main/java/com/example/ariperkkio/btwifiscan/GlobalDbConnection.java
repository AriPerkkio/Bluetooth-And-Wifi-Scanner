package com.example.ariperkkio.btwifiscan;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
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
    final int sendingSize = 100; // Send results in blocks
    HttpResponsePass responseListener;
    private static ProgressDialog uploaderProgressDialog;

    public GlobalDbConnection(Context ctx, HttpResponsePass _responseListener) {
        context = ctx;
        responseListener = _responseListener;
        connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        } else
            Log.e("GlobDbCon constr", "networkinfo: " + (networkInfo != null));
    }

    private class downloader extends AsyncTask<String, Void, String> {
        private String method;
        private ProgressDialog progressDialog = new ProgressDialog(context);
        public downloader(String _method){
            method = _method;
        }

        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid. \n" + e.toString();
            }
        }
        protected void onPreExecute() {
            String message = "";
            switch(method){
                case "getAllBt":
                    message = "Downloading Bluetooth results...";
                break;

                case "getAllWifi":
                    message = "Downloading Wifi results...";
                break;

                case "pingServerOne":
                case "pingServerTwo":
                    message = "Checking status of server";
                break;

                case "pingDbOne":
                case "pingDbTwo":
                case "pingDbThree":
                    message = "Checking status of database";
                break;

                case "countBt":
                case "countWifi":
                    message = "Calculating scan results...";
                break;
            }
            progressDialog.setMessage(message);
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);
        }
        protected void onPostExecute(String result) {
            progressDialog.hide();
            switch(method){
                case "getAllBt":
                    responseListener.scanResultPass("getAllResults", parseBtJson(result));
                break;

                case "getAllWifi":
                    responseListener.scanResultPass("getAllResults", parseWifiJson(result));
                break;

                case "countBt":
                case "countWifi":
                case "pingServerOne":
                case "pingServerTwo":
                case "pingDbOne":
                case "pingDbThree":
                case "pingDbTwo":
                    Log.d("onPost", method+":"+result);
                    responseListener.onResponseRead(result, method);
                break;
            }
        }
    }
    private int resultUpdate = 0;
    private class uploader extends AsyncTask<String, String, String> {
        private String method;
        private String data;
        private int totalResults;

        public void hideProgDiag(){
            uploaderProgressDialog.hide();
            uploaderProgressDialog = null;
        }
        public uploader(String _method, String _data, int _totalResults){
            method = _method;
            data = _data;
            totalResults = _totalResults;
            if(uploaderProgressDialog == null)
                uploaderProgressDialog = new ProgressDialog(context);
        }
        protected String doInBackground(String... urls) {
            try {
                String resultString = postToUrl(urls[0], data);
                publishProgress(resultString);
                return resultString;
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid. \n" + e.toString();
            }
        }

        protected void onProgressUpdate(String... progress) {
            Log.d("onProgressUpd, "+method, progress[0]);
            switch(method){
                case "uploadWifi":
                    String totalNetworks = progress[0].split("/")[2].split(" ")[0];
                    resultUpdate += Integer.parseInt(totalNetworks);
                    uploaderProgressDialog.setProgress(resultUpdate);
                break;
                case "uploadBt":
                    String totalDevices = progress[0].split("/")[1].split(" ")[0];
                    resultUpdate += Integer.parseInt(totalDevices);
                    uploaderProgressDialog.setProgress(resultUpdate);
                break;

                case "syncBt":
                case "syncWifi":
                    //
                break;
            }
        }

        protected void onPreExecute() {
            String message = "";
            switch (method) {
                case "uploadBt":
                case "uploadWifi":
                    message = "Uploading results...";
                    uploaderProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    uploaderProgressDialog.setIndeterminate(false); //Set progress updates visible
                    uploaderProgressDialog.setMax(totalResults);
                    break;

                case "syncBt":
                    message = "Synchronizing  Bluetooth results...";
                break;

                case "syncWifi":
                    message = "Synchronizing  Wifi results...";
                break;

            }
            uploaderProgressDialog.setMessage(message);
            uploaderProgressDialog.setCanceledOnTouchOutside(false);
            uploaderProgressDialog.show();
        }
        protected void onPostExecute(String result) {
            switch(method){
                case "uploadBt":
                case "uploadWifi":
                    responseListener.onResponseRead(result, method);
                break;

                case "syncBt":
                    responseListener.scanResultPass(method, parseBtJson(result));
                break;

                case "syncWifi":
                    responseListener.scanResultPass(method, parseWifiJson(result));
                break;
            }
        }
    }

    public void hideUploaderProgDiag(){
        new uploader("","", 0).hideProgDiag();
    }

    public String downloadUrl(String _url) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            is = conn.getInputStream();
            String returnString = readIt(is);
            conn.disconnect();
            return returnString;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String postToUrl(String _url, String data) throws IOException {
        //Log.d("Data", data.charAt(data.length()-6)+""+data.charAt(data.length()-5)+""+data.charAt(data.length()-4)+""
        //        +data.charAt(data.length()-3)+""+data.charAt(data.length()-2)+""+data.charAt(data.length()-1));
        InputStream is = null;
        try {
            URL url = new URL(_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            System.setProperty("http.keepAlive", "false");
            System.setProperty("http.maxConnections", "25");
            conn.setReadTimeout(100000);
            conn.setConnectTimeout(150000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length", data.length()+"");
            conn.setFixedLengthStreamingMode(data.length());
            //Log.d("Content-Length", data.length()+"");
            conn.setDoInput(true);
            DataOutputStream outputStream = new DataOutputStream((conn.getOutputStream()));
            outputStream.writeBytes(data);
            //Log.i("outputSteam", "Written data length: "+outputStream.size());
            outputStream.flush();
            outputStream.close();
            conn.connect();
            is = conn.getInputStream();
            return readIt(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private String readIt(InputStream stream) throws IOException {
        String returnString = "";
        Reader reader = new InputStreamReader(stream, "UTF-8");
        BufferedReader bfReader = new BufferedReader(reader);
        String newLine;
        while((newLine = bfReader.readLine())!=null)
            returnString += newLine;
        return returnString;
    }

    public List<scanResult> parseBtJson(String inputString) {
        List<scanResult> btDeviceList = new Vector<>();
        String[] btJson = inputString.split("   "); // Each root separated by three spaces
        try {
            for (String btSubJson:btJson){
                JSONObject jsonRootObject = new JSONObject(btSubJson);
                jsonArray = jsonRootObject.optJSONArray("btscans");
                for (int ii = 0; ii < jsonArray.length(); ii++) {
                    if(jsonArray.length()==ii) break; // All results read
                    JSONObject newJsonObject = jsonArray.getJSONObject(ii);
                    btDeviceList.add(new scanResult(
                            (String) newJsonObject.get("devName"),
                            (String) newJsonObject.get("devAddr"),
                            reverseBtType((String) newJsonObject.get("devType")),
                            reverseRssi((String) newJsonObject.get("devRssi")),
                            (String) newJsonObject.get("location")));
                }
            }
        } catch (JSONException e) {
            Log.e("JSON", e.toString());
        }
        return btDeviceList;
    }

    public List<scanResult> parseWifiJson(String inputString) {
        List<scanResult> wifiNetworkList = new Vector<>();
        String[] wifiJson = inputString.split("   "); // Each root separated by three spaces
        try {
            for(String wifiSubJson:wifiJson) {
                JSONObject jsonRootObject = new JSONObject(wifiSubJson);
                jsonArray = jsonRootObject.optJSONArray("wifiscans");
                for (int ii = 0; ii < sendingSize; ii++) {
                    if(jsonArray.length()==ii) break; // All results read
                    JSONObject newJsonObject = jsonArray.getJSONObject(ii);
                    wifiNetworkList.add(new scanResult(
                            (String) newJsonObject.get("ssid"),
                            (String) newJsonObject.get("bssid"),
                            (String) newJsonObject.get("capabilities"),
                            reverseFreq((String) newJsonObject.get("freq")),
                            reverseRssi((String) (newJsonObject.get("rssi"))),
                            (String) newJsonObject.get("location")));
                }
            }
        }catch (JSONException e) {
            Log.e("JSON", e.toString());
        }
        return wifiNetworkList;
    }

    public void upload(List<scanResult> list, String server){
        List<scanResult> btList = new Vector<>();
        JSONObject btResultsJson = new JSONObject();
        JSONArray btArray = new JSONArray();
        JSONObject btJson = new JSONObject();
        List<scanResult> wifiList = new Vector<>();
        JSONObject wifiResultsJson = new JSONObject();
        JSONArray wifiArray = new JSONArray();
        JSONObject wifiJson = new JSONObject();

        for(int i=0;i<list.size();i++){
            if(list.get(i).getTechnology().equals("Bluetooth"))
                btList.add(list.get(i));
            else
                wifiList.add((list.get(i)));
        }

        do {
            try {
                int devCount = 0;
                do {
                    if (btList.size() == 0) break;
                    btResultsJson.put("devName", checkStr(checkName(btList.get(0).getBtDevName())));
                    btResultsJson.put("devAddr", btList.get(0).getBtDevAddr());
                    btResultsJson.put("devType", btList.get(0).getBtDevType());
                    btResultsJson.put("devRssi", btList.get(0).getBtRSSI() + "dBm");
                    btResultsJson.put("location", btList.get(0).getLocation());
                    btArray.put(btResultsJson);
                    btResultsJson = new JSONObject();
                    btList.remove(0);
                    devCount++;
                } while (devCount != sendingSize); // 10 Results in JSON
                btJson.put("btscans", btArray);
                new uploader("uploadBt", btJson.toString(), list.size()).execute(server+"/upload");
            } catch (JSONException e) {
                Log.e("btscans", e.toString());
            }
            btJson.remove("btscans");
            btArray = new JSONArray();
        }while(btList.size()!=0);

        do {
            try {
                int networkCount = 0;
                do {
                    if (wifiList.size() == 0) break;
                    wifiResultsJson.put("ssid", checkStr(checkName(wifiList.get(0).getWifiSSID())));
                    wifiResultsJson.put("bssid", wifiList.get(0).getWifiBSSID());
                    wifiResultsJson.put("capabilities", checkStr(wifiList.get(0).getWifiCapabilities()));
                    wifiResultsJson.put("rssi", wifiList.get(0).getWifiRSSI()+"dBm");
                    wifiResultsJson.put("freq", wifiList.get(0).getWifiFrequency()+"MHz");
                    wifiResultsJson.put("location", wifiList.get(0).getLocation());
                    wifiArray.put(wifiResultsJson);
                    wifiResultsJson = new JSONObject();
                    wifiList.remove(0);
                    networkCount ++;
                } while (networkCount  != sendingSize); // 10 Results in JSON
                wifiJson.put("wifiscans", wifiArray);
                new uploader("uploadWifi", "{\"btscans\":[{}]}"+wifiJson.toString(), list.size()).execute(server+"/upload");
            } catch (JSONException e) {
                Log.e("wifiscans", e.toString());
            }
            wifiJson.remove("wifiscans");
            wifiArray = new JSONArray();
        }while(wifiList.size()!=0);
    }

    public void synchronizeBt(List<scanResult> list, String server){
        Log.d("synchronizeBt", "Results: "+list.size());
        List<scanResult> btList = new Vector<>();
        JSONObject btResultsJson = new JSONObject();
        JSONArray btArray = new JSONArray();
        JSONObject btJson = new JSONObject();

        for(int i=0;i<list.size();i++)
            if(list.get(i).getTechnology().equals("Bluetooth"))
                btList.add(list.get(i));
        final int totalSize = btList.size();
        do {
            try {
                int devCount = 0;
                do {
                    if (btList.size() == 0) break;
                    btResultsJson.put("devName", checkStr(checkName(btList.get(0).getBtDevName())));
                    btResultsJson.put("devAddr", btList.get(0).getBtDevAddr());
                    btResultsJson.put("devType", btList.get(0).getBtDevType());
                    btResultsJson.put("devRssi", btList.get(0).getBtRSSI() + "dBm");
                    btResultsJson.put("location", btList.get(0).getLocation());
                    btArray.put(btResultsJson);
                    btResultsJson = new JSONObject();
                    btList.remove(0);
                    devCount++;
                } while (devCount != totalSize); // All results at once !
                btJson.put("btscans", btArray);
                new uploader("syncBt", btJson.toString(), btList.size()).execute(server+"/syncBt");
            } catch (JSONException e) {
                Log.e("btscans", e.toString());
            }
            btJson.remove("btscans");
            btArray = new JSONArray();
        }while(btList.size()!=0);
    }

    public void synchronizeWifi(List<scanResult> list, String server){
        Log.d("synchronizeWifi", "Results: "+list.size());
        List<scanResult> wifiList = new Vector<>();
        JSONObject wifiResultsJson = new JSONObject();
        JSONArray wifiArray = new JSONArray();
        JSONObject wifiJson = new JSONObject();

        for(int i=0;i<list.size();i++)
            if(list.get(i).getTechnology().equals("Wifi"))
                wifiList.add((list.get(i)));
        final int totalSize = wifiList.size();
        do {
            try {
                int networkCount = 0;
                do {
                    if (wifiList.size() == 0) break;
                    wifiResultsJson.put("ssid", checkStr(checkName(wifiList.get(0).getWifiSSID())));
                    wifiResultsJson.put("bssid", wifiList.get(0).getWifiBSSID());
                    wifiResultsJson.put("capabilities", checkStr(wifiList.get(0).getWifiCapabilities()));
                    wifiResultsJson.put("rssi", wifiList.get(0).getWifiRSSI()+"dBm");
                    wifiResultsJson.put("freq", wifiList.get(0).getWifiFrequency()+"MHz");
                    wifiResultsJson.put("location", wifiList.get(0).getLocation());
                    wifiArray.put(wifiResultsJson);
                    wifiResultsJson = new JSONObject();
                    wifiList.remove(0);
                    networkCount ++;
                } while (networkCount  != totalSize); // All results at once !
                wifiJson.put("wifiscans", wifiArray);
                new uploader("syncWifi", "{\"btscans\":[{}]}"+wifiJson.toString(), wifiList.size()).execute(server+"/syncWifi");
            } catch (JSONException e) {
                Log.e("wifiscans", e.toString());
            }
            wifiJson.remove("wifiscans");
            wifiArray = new JSONArray();
        }while(wifiList.size()!=0);
    }

    public void getAllResults(String server) {
        new downloader("getAllBt").execute(server + "/getAllBt");
        new downloader("getAllWifi").execute(server + "/getAllWifi");
    }

    public void pingServerOne() {
        new downloader("pingServerOne").execute(context.getResources().getString(R.string.servOne));
    }

    public void pingServerTwo() {
        new downloader("pingServerTwo").execute(context.getResources().getString(R.string.servTwo));
    }

    public void pingDbOne(String server) {
        new downloader("pingDbOne").execute(server + "/pingOkeanos");
    }

    public void pingDbTwo(String server) {
        new downloader("pingDbTwo").execute(server + "/pingDigiOcean");
    }

    public void pingDbThree(String server) {
        new downloader("pingDbThree").execute(server + "/pingAzure");
    }

    public void getBtCount(String server) {
        new downloader("countBt").execute(server + "/countBt");
    }

    public void getWifiCount(String server) {
        new downloader("countWifi").execute(server + "/countWifi");
    }

    public int reverseBtType(String type) {
        switch (type) { // Convert int getType() into correct string
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

    public String checkName(String name){
        if(name == null || name.equals(""))
            return " ";
        return name;
    }

    private int reverseRssi(String rssi) {
        return Integer.parseInt(rssi.split("dBm")[0]); // I.e. "-113dbm" -> -113
    }

    private int reverseFreq(String freq) {
        return Integer.parseInt(freq.split("MHz")[0]); // I.e. "2400MHz" -> 2400
    }

    /** temp **/
    // Change '[' and ']' to '(' and ')' so that JSON won't get messed up later
    // Also check for mysql illegal chars
    public String checkStr(String input){
        char[] inputStr = input.toCharArray();
        for(int i=0;i<input.length();i++){
            if(input.charAt(i)=='[')
                inputStr[i] = '(';
            if(input.charAt(i)==']')
                inputStr[i] = ')';
            if(input.charAt(i)=='\'')
                inputStr[i]=' ';
            if(input.charAt(i)=='\\')
                inputStr[i]=' ';
            if(input.charAt(i)=='/')
                inputStr[i]=' ';
            if(input.charAt(i)=='´')
                inputStr[i]=' ';
            if(input.charAt(i)=='¨')
                inputStr[i]=' ';
            if(input.charAt(i)=='ˇ')
                inputStr[i]=' ';
            if(input.charAt(i)=='̧')
                inputStr[i]=' ';
        }
        return String.valueOf(inputStr);
    }
}
