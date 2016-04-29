/*
 * Controller.cpp
 *
 *  Created on: 27.4.2016
 *      Author: AriPerkkio
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <iostream>
#include <sstream>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <vector>

#include "Btresult.h"
#include "Wifiresult.h"
#include "Dao.h"
#include "JsonParser.h"

// Switch case definitions - values don't matter
// CMD
#define GET 101
#define POST 102
// PATH
#define PINGAZURE 201
#define PINGOKEANOS 202
#define PINGDIGIOCEAN 203
#define COUNTWIFI 204
#define COUNTBT 205
#define GETALLBT 206
#define GETALLWIFI 207
#define CLEARALL 299 // TODO: Delete
// CONTENT-TYPE
#define JSONCONTENT 301
#define XMLCONTENT 302

void error(const char *msg) {
    perror(msg);
    exit(1);
}

int processCmd(char _cmd[]){
	if(strcmp(_cmd, "GET") == 0) return GET;
	if(strcmp(_cmd, "POST") == 0) return POST;
	return 0;
}

int processPath(char _path[]){
	if(strcmp(_path, "/pingAzure") == 0) return PINGAZURE;
	if(strcmp(_path, "/pingOkeanos") == 0) return PINGOKEANOS;
	if(strcmp(_path, "/pingDigiOcean") == 0) return PINGDIGIOCEAN;
	if(strcmp(_path, "/countBt") == 0) return COUNTBT;
	if(strcmp(_path, "/countWifi") == 0) return COUNTWIFI;
	if(strcmp(_path, "/getAllBt") == 0) return GETALLBT;
	if(strcmp(_path, "/getAllWifi") == 0) return GETALLWIFI;
	if(strcmp(_path, "/clearAll") == 0) return CLEARALL;
	return 0;
}

int processContentType(char _type[]){
	if(strcmp(_type, "application/json") == 0) return JSONCONTENT;
	if(strcmp(_type, "application/xml") == 0) return XMLCONTENT; // Not tested, XML features not implemented
	return 0;
}

int main(int argc, char **argv) {

	// Connection
	int 		listenfd, connfd, n; // Socket IDs; Listening socket and connected socket. n for indexing loops
	struct 		sockaddr_in servaddr, cliaddr; // Address structure to hold server's and client's address
	char 		inBuff[2048], outBuff[2048], logBuff[2048]; // Data buffers for input and output + logs
	char 		clientBuff[256]; // To hold converted client address
	socklen_t 	len; // Client address info

	// HTTP Request
	char		cmd[16], path[64], type[64]; // HTTP-request details
	std::string header; // HTTP-header

	// JSON parsing
	JsonParser jsonParser;

	// Database
	Dao	dao;

	// Results
	std::vector<Btresult> listBtDevices; // Bluetooth results
	std::vector<Wifiresult> listWifiNetworks; // Wifi results

	if (argc != 3) // Verify number of args
	   error("usage: <Program Name> <IP Addr>  <Port No.>");

	listenfd = socket(AF_INET, SOCK_STREAM, 0); // Create socket

	bzero(&servaddr, sizeof(servaddr)); // Zero and fill in server address structure
	servaddr.sin_family = AF_INET; // TCP
	inet_pton(AF_INET, argv[1], &servaddr.sin_addr); // Text IP to binary format
	servaddr.sin_port = htons(atoi(argv[2])); // Host byte order to network byte order

	bind(listenfd, (struct sockaddr *) &servaddr, sizeof(servaddr)); // Connects the socket to an external interface
	listen(listenfd, 5); // Socket to passive listening

	for ( ; ; ) {
		len = sizeof(cliaddr); // Set len to correct sizeof cliaddr struct.
		connfd = accept(listenfd, (struct sockaddr *) &cliaddr, &len); //Accept connection, save clients address to variable
		// Convert clients address from binary to text form. Save it to clientBuff. Convert clients port from network byte order to host byte order
		snprintf(logBuff, 256, "echo \"\nConnection from: %s : %d \" >> Log.txt", inet_ntop(AF_INET, &cliaddr.sin_addr, clientBuff, sizeof(clientBuff)), ntohs(cliaddr.sin_port));
		system(logBuff); // Append log

		while( (n = read(connfd, inBuff, sizeof(inBuff))) > 0 ) {
			sscanf(inBuff, "%s %s", cmd, path); // Parse HTTP request's command and path
			inBuff[n] = 0; //Null terminate
			if(strstr(inBuff, "\r\n\r\n")) break; // End of HTTP-request
		}

		// Parse Content Type
		std::istringstream req(inBuff);
		while (std::getline(req, header))
			sscanf(header.c_str(), "Content-Type: %s", type); // HTTP-Header: JSON or XML

		int newBt = 0, newWifi = 0;
		string returnString;
		cout << "\n\nRequest details\nCommand: "<< cmd <<"\nContent-Type: "<< type;
		switch(processCmd(cmd)){
			case GET:
				switch(processPath(path)){
					case PINGAZURE:
						if(dao.pingAzure()) snprintf(outBuff, sizeof(outBuff), "Azure-DB ping OK\n");
						else snprintf(outBuff, sizeof(outBuff), "Azure-DB ping FAIL\n");
					break;

					case PINGOKEANOS:
						if(dao.pingOkeanos()) snprintf(outBuff, sizeof(outBuff), "Okeanos-DB ping OK\n");
						else snprintf(outBuff, sizeof(outBuff), "Okeanos-DB ping FAIL\n");
					break;

					case PINGDIGIOCEAN:
						if(dao.pingDigiocean()) snprintf(outBuff, sizeof(outBuff), "DigitalOcean-DB ping OK\n");
						else snprintf(outBuff, sizeof(outBuff), "DigitalOcean-DB ping FAIL\n");
					break;

					case COUNTBT:
						snprintf(outBuff, sizeof(outBuff), "Count of Bluetooth Devices: %d\n", dao.getBtCount());
					break;

					case COUNTWIFI:
						snprintf(outBuff, sizeof(outBuff), "Count of Wifi Networks: %d\n", dao.getWifiCount());
					break;

					case GETALLBT:
						listBtDevices = dao.getAllBtResults();
						snprintf(outBuff, sizeof(outBuff), "%s\n", jsonParser.btResultsToJson(listBtDevices).c_str());
					break;

					case GETALLWIFI:
						listWifiNetworks = dao.getAllWifiResults();
						snprintf(outBuff, sizeof(outBuff), "%s\n", jsonParser.wifiResultsToJson(listWifiNetworks).c_str());
					break;

					case CLEARALL:
						dao.tempClearDb();
						snprintf(outBuff, sizeof(outBuff), "DB Cleared.\nCount of Bluetooth Devices: %d\nCount of Wifi Networks: %d\n", dao.getBtCount(), dao.getWifiCount());
					break;
				} // Path
			break;

			case POST:
				switch(processContentType(type)){
					case JSONCONTENT:
						listBtDevices = jsonParser.parseBtJson(inBuff);
						listWifiNetworks = jsonParser.parseWifiJson(inBuff);
						newBt = dao.insertBtResults(listBtDevices);
						newWifi = dao.insertWifiResults(listWifiNetworks);
						snprintf(outBuff, sizeof(outBuff), "%d/%lu Bluetooth devices and %d/%lu Wifi networks inserted.\n"
												,newBt ,listBtDevices.size(), newWifi, listWifiNetworks.size());
					break;

					case XMLCONTENT:
						printf("Content XML");
						snprintf(outBuff, sizeof(outBuff), "XML Not supported\n");
					break;
				} // Type
			break;
		}// CMD

		if (n < 0)  // Error with data reading
			snprintf(outBuff, sizeof(outBuff), "Error reading data\n");
		if(outBuff[0]==0)
			snprintf(outBuff, sizeof(outBuff), "Successfully read unknown request\n");

		snprintf(logBuff, 256, "echo \"Command: %s\nContent-Type: %s\nReply: %s\n\" >> Log.txt",cmd, type, outBuff);
		system(logBuff); // Append log

		std::cout << "\nSending: " << outBuff << std::endl; // Print out-going data to server
		write(connfd, outBuff, strlen(outBuff)); //wire data to the client
		close(connfd); //Close current connection, loop back to polling connection

		// Clear everything
		memset(&cmd[0], 0, sizeof(cmd));
		memset(&path[0], 0, sizeof(path));
		memset(&type[0], 0, sizeof(type));
		memset(&inBuff[0], 0, sizeof(inBuff));
		memset(&outBuff[0], 0, sizeof(outBuff));
		listBtDevices.clear();
		listWifiNetworks.clear();
		req.clear();
	}
}

/**
curl -H "Content-Type: application/json" -X POST -d @file.json http://localhost:8888
curl -X GET localhost:8888/pingAzure
curl -X GET localhost:8888/pingOkeanos
 **/
