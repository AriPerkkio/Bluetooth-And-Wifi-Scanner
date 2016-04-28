/*
 * Dao.cpp
 *
 *  Created on: 27.4.2016
 *      Author: AriPerkkio
 */

#include "Dao.h"
#include "mysql_connection.h"
#include <cppconn/driver.h>
#include <cppconn/exception.h>
#include <cppconn/resultset.h>
#include <cppconn/statement.h>
#include <stdio.h>
#include <string.h>
#include <sstream>
using namespace std;

Dao::Dao() {
	res = 0, conn = 0, stmt = 0;
	driver = get_driver_instance();
	pingQuery = "SELECT 1";
	readCredentials();
}

// Check status of Azure DB
bool Dao::pingAzure(){
	return pingDb(azureUrl, azureUser, azurePass, azureSchema);
}

// Check status of Okeanos DB
bool Dao::pingOkeanos(){
	return pingDb(okeanosUrl, okeanosUser, okeanosPass, okeanosSchema);
}

// Check status of DigitalOcean DB
bool Dao::pingDigiocean(){
	return pingDb(digioceanUrl, digioceanUser, digioceanPass, digioceanSchema);
}

// Ping specific database
bool Dao::pingDb(string _url, string _user, string _pass, string _schema){
	try {
		conn = driver->connect(_url, _user, _pass);
		conn->setSchema(_schema);
		stmt = conn->createStatement();
		res = stmt->executeQuery(pingQuery);
		res->next();
		delete stmt;
		delete conn;
		if(res->getInt(1)==1){
			delete res;
			return true;
		}
	delete res;
	}catch (sql::SQLException &e) {
		std::cout << " (MySQL error code: " << e.getErrorCode();
		std::cout << ", SQLState: " << e.getSQLState() << " )" << std::endl;
	}
	return false;
}

void Dao::setConnection(string _url, string _user, string _pass, string _schema){
	try {
		conn = driver->connect(_url, _user, _pass);
		conn->setSchema(_schema);
	}catch (sql::SQLException &e) {
		std::cout << " (MySQL error code: " << e.getErrorCode();
		std::cout << ", SQLState: " << e.getSQLState() << " )" << std::endl;
	}
}

// Read database credentials from file into variables
void Dao::readCredentials(){
	FILE 			*fp;
	char 			fileBuff[2048];
	unsigned int	c, count=0;

	fp = fopen("./credentials.txt", "r");
	if(fp==0) printf("Credentials.txt not found");
	else {
		while(1){
			c = fgetc(fp);
			if(feof(fp) || (count>=sizeof(fileBuff))) break; //EOF or full buffer to break loop
			fileBuff[count] = c; //Causes overflows
			count++;
		}
		fclose(fp);	//Close the file
		istringstream fileContent(fileBuff);
		string oneLine;
		while (std::getline(fileContent, oneLine)) {
			char urlBuff[64], userBuff[64], passBuff[64], schemaBuff[64];
			if(sscanf(oneLine.c_str(), "Okeanos: {\"URL\":\"%[^\"]\", \"USER\":\"%[^\"]\", \"PASS\":\"%[^\"]\", \"SCHEMA\":\"%[^\"]\"}", urlBuff, userBuff, passBuff, schemaBuff)==4){
				okeanosUrl = urlBuff; okeanosUser = userBuff; okeanosPass = passBuff; okeanosSchema = schemaBuff;
			}
			if(sscanf(oneLine.c_str(), "Azure: {\"URL\":\"%[^\"]\", \"USER\":\"%[^\"]\", \"PASS\":\"%[^\"]\", \"SCHEMA\":\"%[^\"]\"}", urlBuff, userBuff, passBuff, schemaBuff)==4){
				azureUrl = urlBuff; azureUser = userBuff; azurePass = passBuff; azureSchema = schemaBuff;
			}
			if(sscanf(oneLine.c_str(), "DigiOcean: {\"URL\":\"%[^\"]\", \"USER\":\"%[^\"]\", \"PASS\":\"%[^\"]\", \"SCHEMA\":\"%[^\"]\"}", urlBuff, userBuff, passBuff, schemaBuff)==4){
				digioceanUrl = urlBuff; digioceanUser = userBuff; digioceanPass = passBuff; digioceanSchema = schemaBuff;
			}
		}
	}
	memset(fileBuff, 0, sizeof(fileBuff)); //Empty filedata buffer
}

bool Dao::checkExistingResult(Btresult _btDevice){
	try{
		prep_stmt = conn->prepareStatement("SELECT COUNT(*) FROM BluetoothResults WHERE DeviceAddress = ?");
		prep_stmt->setString(1, _btDevice.getAddress());
		res = prep_stmt->executeQuery();
		res->next();
		delete prep_stmt;
		if(res->getInt(1)!=0){
			delete res;
			return true;
		}
	}catch (sql::SQLException &e) {
		std::cout << " (MySQL error code: " << e.getErrorCode();
		std::cout << ", SQLState: " << e.getSQLState() << " )" << std::endl;
	}
	delete res;
	return false;
}

int Dao::insertBtResults(vector<Btresult> _list){
	int newDevices = 0;
	for(int i=0;i<3;i++){ // Three databases
		for(unsigned int ii=0;ii<_list.size();ii++){ // All results
			try{
				if(i==0) setConnection(okeanosUrl, okeanosUser, okeanosPass, okeanosSchema);
				if(i==1) setConnection(azureUrl, azureUser, azurePass, azureSchema);
				if(i==2) setConnection(digioceanUrl, digioceanUser, digioceanPass, digioceanSchema);
				if(!checkExistingResult(_list.at(ii))){
					printf("\nInserting, i: %d, ii: %u", i, ii);
					prep_stmt = conn->prepareStatement("INSERT INTO BluetoothResults(DeviceName, DeviceAddress, DeviceType, RSSI, Location) VALUES(?, ?, ?, ?,?)");
					prep_stmt->setString(1, _list.at(ii).getName());
					prep_stmt->setString(2, _list.at(ii).getAddress());
					prep_stmt->setString(3, _list.at(ii).getType());
					prep_stmt->setString(4, _list.at(ii).getRssi());
					prep_stmt->setString(5, _list.at(ii).getLoc());
					prep_stmt->executeQuery();
					delete prep_stmt;
					newDevices++;
				}
				delete conn;
			}catch (sql::SQLException &e) {
				std::cout << " (MySQL error code: " << e.getErrorCode();
				std::cout << ", SQLState: " << e.getSQLState() << " )" << std::endl;
			}
		}
	}
	return newDevices;
}

bool Dao::checkExistingResult(Wifiresult _wifiNetwork){
	try{
		prep_stmt = conn->prepareStatement("SELECT COUNT(*) FROM WifiResults WHERE BSSID = ?");
		prep_stmt->setString(1, _wifiNetwork.getBssid());
		res = prep_stmt->executeQuery();
		res->next();
		delete prep_stmt;
		if(res->getInt(1)!=0){
			delete res;
			return true;
		}
	}catch (sql::SQLException &e) {
		std::cout << " (MySQL error code: " << e.getErrorCode();
		std::cout << ", SQLState: " << e.getSQLState() << " )" << std::endl;
	}
	delete res;
	return false;
}

int Dao::insertWifiResults(vector<Wifiresult> _list){
	int newNetworks = 0;
	for(int i=0;i<3;i++){ // Three databases
		for(unsigned int ii=0;ii<_list.size();ii++){ // All results
			try{
				if(i==0) setConnection(okeanosUrl, okeanosUser, okeanosPass, okeanosSchema);
				if(i==1) setConnection(azureUrl, azureUser, azurePass, azureSchema);
				if(i==2) setConnection(digioceanUrl, digioceanUser, digioceanPass, digioceanSchema);
				if(!checkExistingResult(_list.at(ii))){
					printf("\nInserting, i: %d, ii: %u", i, ii);
					prep_stmt = conn->prepareStatement("INSERT INTO WifiResults(SSID, BSSID, Capabilities, RSSI, Frequency, Location) VALUES(?, ?, ?, ?, ?,?)");
					prep_stmt->setString(1, _list.at(ii).getSsid());
					prep_stmt->setString(2, _list.at(ii).getBssid());
					prep_stmt->setString(3, _list.at(ii).getCap());
					prep_stmt->setString(4, _list.at(ii).getRssi());
					prep_stmt->setString(5, _list.at(ii).getFreq());
					prep_stmt->setString(6, _list.at(ii).getLoc());
					prep_stmt->executeQuery();
					delete prep_stmt;
					newNetworks++;
				}
				delete conn;
			}catch (sql::SQLException &e) {
				std::cout << " (MySQL error code: " << e.getErrorCode();
				std::cout << ", SQLState: " << e.getSQLState() << " )" << std::endl;
			}
		}
	}
	return newNetworks;
}

Dao::~Dao() {
	// TODO Auto-generated destructor stub
}

