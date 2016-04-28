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
			printf("\nScanf: %s", oneLine.c_str());
			char urlBuff[64], userBuff[64], passBuff[64], schemaBuff[64];
			if(sscanf(oneLine.c_str(), "Okeanos: {\"URL\":\"%[^\"]\", \"USER\":\"%[^\"]\", \"PASS\":\"%[^\"]\", \"SCHEMA\":\"%[^\"]\"}", urlBuff, userBuff, passBuff, schemaBuff)==4){
				okeanosUrl = urlBuff; okeanosUser = userBuff; okeanosPass = passBuff; okeanosSchema = schemaBuff;
			}
			if(sscanf(oneLine.c_str(), "Azure: {\"URL\":\"%[^\"]\", \"USER\":\"%[^\"]\", \"PASS\":\"%[^\"]\", \"SCHEMA\":\"%[^\"]\"}", urlBuff, userBuff, passBuff, schemaBuff)==4){
				azureUrl = urlBuff; azureUser = userBuff; azurePass = passBuff; azureSchema = schemaBuff;
			}
		}
	}
	memset(fileBuff, 0, sizeof(fileBuff)); //Empty filedata buffer
}

Dao::~Dao() {
	// TODO Auto-generated destructor stub
}

