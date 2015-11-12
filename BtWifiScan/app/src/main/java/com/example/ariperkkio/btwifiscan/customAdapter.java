package com.example.ariperkkio.btwifiscan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by arska on 04/11/15.
 */

// ArrayAdapter can consist of both Bt device and Wifi results
// Checks technology before setting text fields
public class customAdapter extends ArrayAdapter<scanResult>  {
    public customAdapter(Context content, ArrayList<scanResult> scanresult) {
        super(content, 0, scanresult); //Use super class's constructor
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.scanrow, parent, false);
        }

        scanResult scanresult = getItem(position); //Get item form current position

        // See XML for clarification about ids
        ImageView icon = (ImageView) convertView.findViewById(R.id.scanrowIcon);
        TextView fieldOne = (TextView) convertView.findViewById(R.id.scanrowOne);
        TextView fieldTwo = (TextView) convertView.findViewById(R.id.scanrowTwo);
        TextView fieldThree = (TextView) convertView.findViewById(R.id.scanrowThree);
        TextView fieldFour = (TextView) convertView.findViewById(R.id.scanrowFour);
        TextView fieldFive = (TextView) convertView.findViewById(R.id.scanrowFive);
        TextView fieldSix = (TextView) convertView.findViewById(R.id.scanrowSix);

        if(scanresult != null) {
            if(scanresult.technology.equals("Bluetooth")) {
                icon.setImageResource(R.drawable.bticon); // Icon from: http://www.icon2s.com/wp-content/uploads/2012/09/bluetooth_128x128-32.png
                fieldOne.setText(scanresult.btDevName);
                fieldTwo.setText("");
                fieldThree.setText(scanresult.btDevAddr);
                fieldFour.setText(scanresult.btDevType);
                fieldFive.setText("");
                if(scanresult.btRSSI==0) // btRSSI is 0 when user unchecks it from newScanActivity
                    fieldSix.setText("");
                else
                    fieldSix.setText("RSSI: " + scanresult.btRSSI+" dBm"); // crashes when only int as text
            }

            if(scanresult.technology.equals("Wifi")) {
                icon.setImageResource(R.drawable.wifiicon); // Icon from: http://iconbug.com/download/size/512/icon/5294/wireless-silver/
                fieldOne.setText(scanresult.wifiSSID);
                fieldTwo.setText(scanresult.wifiBSSID);
                fieldThree.setText(scanresult.wifiCapabilities);
                fieldFour.setText("");
                if(scanresult.wifiFrequency==0) // wifiFrequency is 0 when user unchecks it from newScanActivity
                    fieldFive.setText("");
                else
                    fieldFive.setText(scanresult.wifiFrequency + " MHz");
                if(scanresult.wifiRSSI==0) // wifiRSSI is 0 when user unchecks it from newScanActivity
                    fieldSix.setText("");
                else
                    fieldSix.setText("RSSI: "+scanresult.wifiRSSI+" dbm"); // crashes when only int as text
            }
        }
        return convertView;
    }
}




