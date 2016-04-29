/*
 * Btresult.h
 *
 *  Created on: 27.4.2016
 *      Author: AriPerkkio
 */

#ifndef BTRESULT_H_
#define BTRESULT_H_
#include <iostream> // for std
using namespace std;

class Btresult {
	string name;
	string address;
	string type;
	string rssi;
	string location;
public:
	Btresult(string, string, string, string, string);
	virtual ~Btresult();
	void printInfo();
	string getName();
	string getAddress();
	string getType();
	string getRssi();
	string getLoc();
	string toString();
};

#endif /* BTRESULT_H_ */
