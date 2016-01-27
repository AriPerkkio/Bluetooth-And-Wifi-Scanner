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

public class scanMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int btCount, wifiCount;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btCount = getIntent().getExtras().getInt("BtCount");
        wifiCount = getIntent().getExtras().getInt("WifiCount");

        if(getIntent().getExtras().getString("Caller") != null && getIntent().getExtras().getString("Caller").equals("class com.example.ariperkkio.btwifiscan.subPrevScanActivity"))
            subPrevScanActivity.mapsProgressDialog.hide();
        if(getIntent().getExtras().getString("Caller") != null && getIntent().getExtras().getString("Caller").equals("class com.example.ariperkkio.btwifiscan.scanActivity"))
            scanActivity.mapsProgressDialog.hide();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        for(int i = 0; i<wifiCount;i++){
            String wifiNetwork = getIntent().getExtras().getString("wifi "+i);
            if(wifiNetwork!=null) {
                String[] attributes = wifiNetwork.split("\n");
                String[] location = attributes[5].split(",");

                latitude = Double.parseDouble(location[0]);
                longitude = Double.parseDouble(location[1]);
                mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                        .title("SSID: "+attributes[0])
                        .snippet("BSSID: "+attributes[1] + "\n" +
                                "Capabilities: "+attributes[2] + "\n" +
                                "Frequency: "+attributes[3] + "MHz\n" +
                                "RSSI: "+attributes[4] + "dbm")
                        .icon((BitmapDescriptorFactory.fromResource(R.mipmap.wifiicon))));
            }
        }

        for(int i = 0; i<btCount;i++){
            String btDevice = getIntent().getExtras().getString("bluetooth "+i);
            if(btDevice != null) {
                String[] attributes = btDevice.split("\n");
                String[] location = attributes[4].split(",");

                latitude = Double.parseDouble(location[0]);
                longitude = Double.parseDouble(location[1]);
                mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                        .title("Name: "+attributes[0])
                        .snippet("Address: "+attributes[1] + "\n" +
                                "Type: "+attributes[2] + "\n" +
                                "RSSI: "+attributes[3] + "dBm")
                        .icon((BitmapDescriptorFactory.fromResource(R.mipmap.bticon))));
            }
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

                Context context = getApplicationContext(); //or getActivity(), YourActivity.this, etc.

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
