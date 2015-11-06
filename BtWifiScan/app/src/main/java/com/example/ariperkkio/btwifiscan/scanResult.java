package com.example.ariperkkio.btwifiscan;

/**
 * Created by arska on 04/11/15.
 */
public class scanResult {

    // Specifies if wifi or bt
    public String technology;

    // Bt attributes
    public String btDevName;
    public String btDevAddr;
    public String btDevType;
    public int btRSSI;

    // booleans to check options for wifi
    public String wifiSSID;
    public String wifiBSSID;
    public String wifiCapabilities;
    public int wifiFrequency;
    public int wifiRSSI;

    // Constructoor for Bt
    public scanResult(String btDevName, String btDevAddr, int btDevType, int btRSSI) {
        this.technology = "Bluetooth";
        this.btDevName = btDevName;
        this.btDevAddr = btDevAddr;
        this.btRSSI = btRSSI;

        switch(btDevType) { // Convert int getType() into correct string
            case(0):
                this.btDevType = "Unknown";
            break;

            case(1):
                this.btDevType = "Classic";
            break;

            case(2):
                this.btDevType = "Low Energy";
            break;

            case(3):
                this.btDevType = "Dual Mode";
            break;

            case(4): // This is set when user unchecks Device Type from newScanActivity
                this.btDevType = "";
            break;
        }
    }

    // Constructoor for Wifi
    public scanResult(String wifiSSID, String wifiBSSID, String wifiCapabilities, int wifiFrequency, int wifiRSSI) {
        this.technology = "Wifi";
        this.wifiSSID = wifiSSID;
        this.wifiBSSID = wifiBSSID;
        this.wifiCapabilities = wifiCapabilities;
        this.wifiFrequency = wifiFrequency;
        this.wifiRSSI = wifiRSSI;
    }

    public String getTechnology() {
        return technology;
    }

    public String getBtDevName() {
        return btDevName;
    }

    public String getBtDevAddr() {
        return btDevAddr;
    }

    public String getBtDevType() {
        return btDevType;
    }

    public int getBtRSSI() {
        return btRSSI;
    }

    public String getWifiSSID() {
        return wifiSSID;
    }

    public String getWifiBSSID() {
        return wifiBSSID;
    }

    public String getWifiCapabilities() {
        return wifiCapabilities;
    }

    public int getWifiFrequency() {
        return wifiFrequency;
    }

    public int getWifiRSSI() {
        return wifiRSSI;
    }
}
