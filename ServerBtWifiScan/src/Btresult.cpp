/*
 * Btresult.cpp
 *
 *  Created on: 27.4.2016
 *      Author: AriPerkkio
 */

#include "Btresult.h"
#include <iostream> // for std
using namespace std;



Btresult::Btresult(string _name, string _addr,string _type, string _rssi, string _loc) {
	this->name = _name;
	this->address = _addr;
	this->type = _type;
	this->rssi = _rssi;
	this->location = _loc;
}

void Btresult::printInfo(){
	cout << "\nName: " << this->name
		 << "\nAddr: " << this->address
		 << "\nType: " << this->type
		 << "\nRSSI: " << this->rssi
		 << "\nLocation: " << this->location
		 << endl;
}

Btresult::~Btresult() {
	// TODO Auto-generated destructor stub
}

