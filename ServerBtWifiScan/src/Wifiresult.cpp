/*
 * Wifiresult.cpp
 *
 *  Created on: 27.4.2016
 *      Author: AriPerkkio
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

string Wifiresult::getSsid(){ return this->ssid; }
string Wifiresult::getBssid(){ return this->bssid; }
string Wifiresult::getCap(){ return this->capabilities; }
string Wifiresult::getRssi(){ return this->rssi; }
string Wifiresult::getFreq(){ return this->frequency; }
string Wifiresult::getLoc(){ return this->location; }

string Wifiresult::toString(){
	return string(this->ssid+", "+this->bssid+", "+this->capabilities+", "+this->rssi+", "+this->frequency+", "+this->location+"\n");
}

// BSSID should always be unique
bool Wifiresult::operator==(const Wifiresult &first) const{
	return this->bssid.compare(first.bssid)==0;
}

Wifiresult::~Wifiresult() {
	// TODO Auto-generated destructor stub
}
