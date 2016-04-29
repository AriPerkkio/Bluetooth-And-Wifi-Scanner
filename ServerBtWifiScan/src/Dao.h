/*
 * Dao.h
 *
 *  Created on: 27.4.2016
 *      Author: AriPerkkio
 */

#ifndef DAO_H_
#define DAO_H_
#include "mysql_connection.h"
#include <cppconn/driver.h>
#include <cppconn/exception.h>
#include <cppconn/resultset.h>
#include <cppconn/statement.h>
#include <cppconn/prepared_statement.h>
#include <iostream>
#include <vector>
#include "Btresult.h"
#include "Wifiresult.h"
using namespace std;
class Dao {
	// SQL Connection
	sql::Driver *driver;
	sql::Connection *conn;
	sql::Statement *stmt;
	sql::ResultSet *res;
	sql::PreparedStatement *prep_stmt;
	// Okeanos DB credentials
	string okeanosUrl;
	string okeanosUser;
	string okeanosPass;
	string okeanosSchema;
	// Azure DB credentials
	string azureUrl;
	string azureUser;
	string azurePass;
	string azureSchema;
	// DigitalOcean DB credentials
	string digioceanUrl;
	string digioceanUser;
	string digioceanPass;
	string digioceanSchema;
	// Queries
	string pingQuery;
	string btCountQuery;
	string wifiCountQuery;
	string btGetAllQuery;
	string wifiGetAllQuery;
	bool pingDb(string, string, string, string);
	void readCredentials();
	void setConnection(string, string, string, string);
	bool checkExistingResult(Btresult);
	bool checkExistingResult(Wifiresult);
	void priorityConnect();
public:
	Dao();
	virtual ~Dao();
	bool pingAzure();
	bool pingOkeanos();
	bool pingDigiocean();
	int insertBtResults(vector<Btresult>);
	int insertWifiResults(vector<Wifiresult>);
	int getBtCount();
	int getWifiCount();
	void tempClearDb(); // TODO: Delete
	vector<Btresult> getAllBtResults();
	vector<Wifiresult> getAllWifiResults();
};

#endif /* DAO_H_ */
