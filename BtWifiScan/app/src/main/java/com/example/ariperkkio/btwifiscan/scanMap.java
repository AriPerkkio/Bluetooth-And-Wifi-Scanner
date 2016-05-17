package com.example.ariperkkio.btwifiscan;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class scanMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(getIntent().getExtras().getString("Caller") != null && getIntent().getExtras().getString("Caller").equals("class com.example.ariperkkio.btwifiscan.subPrevScanActivity"))
            subPrevScanActivity.mapsProgressDialog.hide();
        if(getIntent().getExtras().getString("Caller") != null && getIntent().getExtras().getString("Caller").equals("class com.example.ariperkkio.btwifiscan.scanActivity"))
            scanActivity.mapsProgressDialog.hide();
        if(getIntent().getExtras().getString("Caller") != null && getIntent().getExtras().getString("Caller").equals("class com.example.ariperkkio.btwifiscan.GlobalActivity"))
            GlobalActivity.mapsProgressDialog.hide();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        ResultListHolder resultListHolder = ResultListHolder.getInstance();
        List<scanResult> results = resultListHolder.getResults();

        for(int i=0;i<results.size();i++){
            scanResult result = results.get(i);
            String[] location = result.getLocation().split(",");
            latitude = Double.parseDouble(location[0]);
            longitude = Double.parseDouble(location[1]);

            if(result.getTechnology().equals("Bluetooth") && latitude != 0.0 && longitude != 0.0)
                    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                            .title("Name: "+result.getBtDevName())
                            .snippet("Address: "+result.getBtDevAddr() + "\n" +
                                    "Type: "+result.getBtDevType() + "\n" +
                                    "RSSI: "+result.getBtRSSI() + "dBm")
                            .icon((BitmapDescriptorFactory.fromResource(R.drawable.btmarker))));
            else // Wifi Result
                if(latitude != 0.0 && longitude != 0.0)
                    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                        .title("SSID: "+result.getWifiSSID())
                        .snippet("BSSID: "+result.getWifiBSSID() + "\n" +
                                "Capabilities: "+result.getWifiCapabilities() + "\n" +
                                "Frequency: "+result.getWifiFrequency() + "MHz\n" +
                                "RSSI: "+result.getWifiRSSI() + "dbm")
                        .icon((BitmapDescriptorFactory.fromResource(R.drawable.wifimarker))));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 13));
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                Context context = getApplicationContext();

                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
    }


}
