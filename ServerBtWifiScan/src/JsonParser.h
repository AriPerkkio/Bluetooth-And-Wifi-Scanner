/*
 * JsonParser.h
 *
 *  Created on: 28.4.2016
 *      Author: arska
 */

#ifndef JSONPARSER_H_
#define JSONPARSER_H_
#include <vector>
#include <sstream>
#include "Btresult.h"
#include "Wifiresult.h"

class JsonParser {
public:
	JsonParser();
	virtual ~JsonParser();
	vector<Btresult> parseBtJson(char[], int buffSize);
	vector<Wifiresult> parseWifiJson(char[], int buffSize);
	std::string btResultsToJson(vector<Btresult>);
	std::string wifiResultsToJson(vector<Wifiresult>);
};

#endif /* JSONPARSER_H_ */
