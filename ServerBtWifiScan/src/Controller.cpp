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
#define GET 101
#define POST 102
#define PINGAZURE 201
#define PINGOKEANOS 202
#define PINGDIGIOCEAN 203
#define JSONCONTENT 301
#define XMLCONTENT 302

void error(const char *msg) {
    perror(msg);
    exit(1);
}

int processCmd(char _cmd[]){
	if(strcmp(_cmd, "GET") == 0)
		return GET;
	if(strcmp(_cmd, "POST") == 0)
			return POST;
	return 0;
}

int processPath(char _path[]){
	if(strcmp(_path, "/pingAzure") == 0)
		return PINGAZURE;
	if(strcmp(_path, "/pingOkeanos") == 0)
			return PINGOKEANOS;
	if(strcmp(_path, "/pingDigiOcean") == 0)
			return PINGDIGIOCEAN;
	return 0;
}

int processContentType(char _type[]){
	if(strcmp(_type, "application/json") == 0)
		return JSONCONTENT;
	if(strcmp(_type, "application/xml") == 0) // Not tested
			return XMLCONTENT;
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

		printf("\n\nRequest details \nCommand: %s\nContent-Type: %s\n", cmd, type);
		switch(processCmd(cmd)){
			case GET:
				switch(processPath(path)){
					case PINGAZURE:
						if(dao.pingAzure()) printf("Ping OK");
						else printf("Ping FAIL");
					break;

					case PINGOKEANOS:
						if(dao.pingOkeanos()) printf("Ping OK");
						else printf("Ping FAIL");
					break;

					case PINGDIGIOCEAN:
						if(dao.pingDigiocean()) printf("Ping OK");
						else printf("Ping FAIL");
					break;
				}
			break;

			case POST:
				switch(processContentType(type)){
					case JSONCONTENT:
						listBtDevices = jsonParser.parseBtJson(inBuff);
						listWifiNetworks = jsonParser.parseWifiJson(inBuff);
					break;

					case XMLCONTENT:
						printf("Content XML");
					break;
				}

				for(unsigned int i=0;i<listBtDevices.size();i++)
					listBtDevices.at(i).printInfo();

				for(unsigned int i=0;i<listWifiNetworks.size();i++)
					listWifiNetworks.at(i).printInfo();

				printf("\nControl: Number of BT: %lu", listBtDevices.size());
				printf("\nControl: Number of Wifi: %lu", listWifiNetworks.size());
				break;
		}

		snprintf(logBuff, 256, "echo \"Command: %s\nContent-Type: %s\" >> Log.txt",cmd, type);
		system(logBuff); // Append log

		if (n < 0)  // Error with data reading
			snprintf(outBuff, sizeof(outBuff), "Error reading data\n");
		else
			snprintf(outBuff, sizeof(outBuff), "Successfully read request\n");

		printf("\nSending: %s\n", outBuff ); // Print out-going data to server
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
