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
#include <iostream>
using namespace std;
class Dao {
	// SQL Connection
	sql::Driver *driver;
	sql::Connection *conn;
	sql::Statement *stmt;
	sql::ResultSet *res;
	// Okeanos DB credentials
	std::string okeanosUrl;
	std::string okeanosUser;
	std::string okeanosPass;
	std::string okeanosSchema;
	// Azure DB credentials
	std::string azureUrl;
	std::string azureUser;
	std::string azurePass;
	std::string azureSchema;
	// DigitalOcean DB credentials
	std::string digioceanUrl;
	std::string digioceanUser;
	std::string digioceanPass;
	std::string digioceanSchema;
	// Ping query
	std::string pingQuery;
	bool pingDb(string, string, string, string);
	void readCredentials();

public:
	Dao();
	virtual ~Dao();
	bool pingAzure();
	bool pingOkeanos();
	bool pingDigiocean();
};

#endif /* DAO_H_ */
