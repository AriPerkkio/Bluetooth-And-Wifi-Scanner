/*
 * Btresult.cpp
 *
 *  Created on: 27.4.2016
 *      Author: AriPerkkio
 */

#include "Btresult.h"
#include <iostream> // for std
#include <string>
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

string Btresult::getName(){	return this->name; }
string Btresult::getAddress(){	return this->address; }
string Btresult::getType() { return this->type; }
string Btresult::getRssi() { return this->rssi; }
string Btresult::getLoc() { return this->location; }

string Btresult::toString(){
	return string(this->name+", "+this->address+", "+this->type+", "+this->rssi+", "+this->location+"\n");
}
// Address should always be unique
bool Btresult::operator==(const Btresult &first) const{
	return this->address.compare(first.address)==0;
}

Btresult::~Btresult() {
	// TODO Auto-generated destructor stub
}

