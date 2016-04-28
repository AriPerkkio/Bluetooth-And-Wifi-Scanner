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
	vector<Btresult> parseBtJson(char[]);
	vector<Wifiresult> parseWifiJson(char[]);
};

#endif /* JSONPARSER_H_ */
