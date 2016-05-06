/*
 * Wifiresult.h
 *
 *  Created on: 27.4.2016
 *      Author: AriPerkkio
 */

#ifndef WIFIRESULT_H_
#define WIFIRESULT_H_
#include <iostream>
using namespace std;

class Wifiresult {
	string ssid;
	string bssid;
	string capabilities;
	string rssi;
	string frequency;
	string location;
public:
	Wifiresult(string, string, string, string, string, string);
	virtual ~Wifiresult();
	void printInfo();
	string getSsid();
	string getBssid();
	string getCap();
	string getRssi();
	string getFreq();
	string getLoc();
	string toString();
	bool operator==(const Wifiresult &first) const;
};

#endif /* WIFIRESULT_H_ */

