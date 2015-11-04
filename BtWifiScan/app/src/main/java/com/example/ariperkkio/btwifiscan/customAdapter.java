package com.example.ariperkkio.btwifiscan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arska on 04/11/15.
 */

public class customAdapter extends ArrayAdapter<scanResult>  {
    public customAdapter(Context content, ArrayList<scanResult> scanresult) {
        super(content, 0, scanresult); //Use super class's constructor
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView; //Use old view if possible - don't make things twice!

        if(row == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext()); // Method getLayoutInflater() not working when not nested class
            row = inflater.inflate(R.layout.scanrow, parent, false);
        }

        scanResult scanresult = getItem(position); //Get item form current position

        TextView fieldOne = (TextView) convertView.findViewById(R.id.scanrowOne);
        TextView fieldTwo = (TextView) convertView.findViewById(R.id.scanrowTwo);
        TextView fieldThree = (TextView) convertView.findViewById(R.id.scanrowThree);
        TextView fieldFour = (TextView) convertView.findViewById(R.id.scanrowFour);
        TextView fieldFive = (TextView) convertView.findViewById(R.id.scanrowFive);
        TextView fieldSix = (TextView) convertView.findViewById(R.id.scanrowSix);

        if(scanresult != null) {
            if(scanresult.technology.equals("Bluetooth")) {
                fieldOne.setText(scanresult.btDevName);
                fieldTwo.setText(scanresult.btDevAddr);
            }

            if(scanresult.technology.equals("Wifi")) {
                fieldOne.setText(scanresult.wifiSSID);
                fieldTwo.setText(scanresult.wifiBSSID);
            }
        }

        return row;
    }
}




