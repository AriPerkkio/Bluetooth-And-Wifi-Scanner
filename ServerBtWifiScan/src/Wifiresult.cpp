/*
 * Wifiresult.cpp
 *
 *  Created on: 27.4.2016
 *      Author: arska
 */

#include "Wifiresult.h"
#include <iostream>
using namespace std;

Wifiresult::Wifiresult(string _ssid, string _bssid, string _cap, string _rssi, string _freq, string _loc) {
	this->ssid = _ssid;
	this->bssid = _bssid;
	this->capabilities = _cap;
	this->rssi = _rssi;
	this->frequency = _freq;
	this->location = _loc;
}

void Wifiresult::printInfo(){
	cout << "\nSSID: " << this->ssid
		 << "\nBSSID: " << this->bssid
		 << "\nCapabilities: " << this->capabilities
		 << "\nRSSI: " << this->rssi
		 << "\nFrequency: " << this->frequency
		 << "\nLocation: " << this->location
		 << endl;
}

Wifiresult::~Wifiresult() {
	// TODO Auto-generated destructor stub
}
