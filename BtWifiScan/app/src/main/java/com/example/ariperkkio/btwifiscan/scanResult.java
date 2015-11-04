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
    public String wifiFrequency;
    public int wifiRSSI;

    // Constructoor for Bt
    public scanResult(String btDevName, String btDevAddr, String btDevType, int btRSSI) {
        this.technology = "Bluetooth";
        this.btDevName = btDevName;
        this.btDevAddr = btDevAddr;
        this.btDevType = btDevType;
        this.btRSSI = btRSSI;
    }

    // Constructoor for Wifi
    public scanResult(String wifiSSID, String wifiBSSID, String wifiCapabilities, String wifiFrequency, int wifiRSSI) {
        this.technology = "Wifi";
        this.wifiSSID = wifiSSID;
        this.wifiBSSID = wifiBSSID;
        this.wifiCapabilities = wifiCapabilities;
        this.wifiFrequency = wifiFrequency;
        this.wifiRSSI = wifiRSSI;
    }
}
