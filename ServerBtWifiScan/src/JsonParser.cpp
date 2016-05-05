/*
 * JsonParser.cpp
 *
 *  Created on: 28.4.2016
 *      Author: arska
 */

#include "JsonParser.h"
#include <vector>
#include <sstream>
#include <stdio.h>
#include <string.h>
#include "Btresult.h"
#include "Wifiresult.h"

char		btInitialJsonParse[] = "{\"btscans\":[%[^]]%n", wifiInitialJsonParse[] = "{\"wifiscans\":[%[^]]";
char		btJsonParse[] = "\"devName\":\"%[^\"]\", \"devAddr\":\"%[^\"]\", \"devType\":\"%[^\"]\", \"devRssi\":\"%[^\"]\", \"location\":\"%[^\"]\"";
char		wifiJsonParse[] = "\"ssid\":\"%[^\"]\", \"bssid\":\"%[^\"]\", \"capabilities\":\"%[^\"]\", \"rssi\":\"%[^\"]\", \"freq\":\"%[^\"]\", \"location\":\"%[^\"]\"";
char		btdevjson[4096], devName[256], devAddr[64], devType[64], devRssi[64], btloc[64];
char		wifinetjson[4096], ssid[256], bssid[64], capabilities[64], wifiRssi[64], freq[64], wifiloc[64];
int 		offset = 0, pos;
std::vector<Wifiresult> _listWifiNetworks;
std::vector<Btresult> _listBtDevices;

JsonParser::JsonParser() {
	// TODO Auto-generated constructor stub
}
vector<Btresult> JsonParser::parseBtJson(char _buff[], int buffSize){
	char btjson[buffSize];
	cout << "\njsonParser btbuffer size: "<<sizeof(btjson)<<"\n";
	_listBtDevices.clear();
	std::string header;
	std::istringstream req(_buff);
	while (std::getline(req, header)) {
		int readChars = 0;
		sscanf(header.c_str(), btInitialJsonParse, btjson, &readChars); // JSON for all bt results
	}
	offset = 0;
	// Parse btJson into bt results
	while(sscanf(btjson+offset, "{%[^}]%n", btdevjson, &pos)!=0) {
		if(btdevjson[0]==0) break; // TODO: Better way for checking empty
		offset += pos+2;
		if(sscanf(btdevjson, btJsonParse, devName, devAddr, devType, devRssi, btloc) == 5) // All five successful
			_listBtDevices.push_back(Btresult(devName, devAddr, devType, devRssi, btloc)); // Add new bt result
		memset(btdevjson, 0, sizeof(btdevjson));
	}
	memset(btjson, 0, sizeof(btjson));
	return _listBtDevices;
}

vector<Wifiresult> JsonParser::parseWifiJson(char _buff[], int buffSize){
	char btjson[buffSize];
	char wifijson[buffSize];
	cout << "\njsonParser wifibuffer size: "<<sizeof(wifijson)<<"\n";
	_listWifiNetworks.clear();
	std::string header;
	std::istringstream req(_buff);
	while (std::getline(req, header)) {
		int readChars = 0;
		sscanf(header.c_str(), btInitialJsonParse, btjson, &readChars); // JSON for all bt results
		sscanf(header.c_str()+readChars+2, wifiInitialJsonParse, wifijson); // JSON for all wifi results
	}
	// Parse WifiJson into wifi results
	offset = 0;
	while(sscanf(wifijson+offset, "{%[^}]%n", wifinetjson, &pos)!=0){
		if(wifinetjson[0]==0) break; // TODO: Better way for checking empty
		offset += pos+2;
		if(sscanf(wifinetjson, wifiJsonParse, ssid, bssid, capabilities, wifiRssi, freq, wifiloc) == 6) // All six successful
			_listWifiNetworks.push_back(Wifiresult(ssid, bssid, capabilities, wifiRssi, freq, wifiloc)); // Add wifi result
		memset(wifinetjson, 0, sizeof(wifinetjson));
	}
	memset(btjson, 0, sizeof(btjson));
	memset(wifijson, 0, sizeof(wifijson));
	return _listWifiNetworks;
}

std::string JsonParser::btResultsToJson(vector<Btresult> _list){
	std::string returnJson = "{\"btscans\":[\n";
	char devRow[1024];
	for(unsigned long int i = 0;i<_list.size();i++){
		snprintf(devRow, sizeof(devRow), "{\"devName\":\"%s\", \"devAddr\":\"%s\", \"devType\":\"%s\", \"devRssi\":\"%s\", \"location\":\"%s\"}",
				_list.at(i).getName().c_str(), _list.at(i).getAddress().c_str(), _list.at(i).getType().c_str(), _list.at(i).getRssi().c_str(), _list.at(i).getLoc().c_str());
		returnJson.append(devRow);
		if(i+1!=_list.size()) returnJson.append(",\n"); // Not last row
		memset(devRow, 0, sizeof(devRow));
	}
	returnJson.append("\n]}");
	return returnJson;
}

std::string JsonParser::wifiResultsToJson(vector<Wifiresult> _list){
	std::string returnJson = "{\"wifiscans\":[\n";
	char netRow[1024];
	for(unsigned long int i = 0;i<_list.size();i++){
		snprintf(netRow, sizeof(netRow),
				"{\"ssid\":\"%s\", \"bssid\":\"%s\", \"capabilities\":\"%s\", \"rssi\":\"%s\", \"freq\":\"%s\", \"location\":\"%s\"}",
				_list.at(i).getSsid().c_str(), _list.at(i).getBssid().c_str(), _list.at(i).getCap().c_str(), _list.at(i).getRssi().c_str(), _list.at(i).getFreq().c_str(), _list.at(i).getLoc().c_str());
		returnJson.append(netRow);
		if(i+1!=_list.size()) returnJson.append(",\n"); // Not last row
		memset(netRow, 0, sizeof(netRow));
	}
	returnJson.append("\n]}");
	return returnJson;
}

JsonParser::~JsonParser() {
	// TODO Auto-generated destructor stub
}

