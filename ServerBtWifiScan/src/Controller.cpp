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
#define SYNCBT 208
#define SYNCWIFI 209
#define UPLOAD 210
#define CLEARALL 299
// CONTENT-TYPE
#define JSONCONTENT 301
#define XMLCONTENT 302

// Connection constants
#define BUFFER 4096
#define RESULTRESP 7 // Number of results sent in one HTTP response

// Connection
int 		listenfd, connfd, n; // Socket IDs; Listening socket and connected socket. n for indexing loops
struct 		sockaddr_in servaddr, cliaddr; // Address structure to hold server's and client's address
char 		inBuff[BUFFER], outBuff[BUFFER], logBuff[BUFFER]; // Data buffers for input and output + logs
char 		clientBuff[256]; // To hold converted client address
socklen_t 	len; // Client address info

// HTTP Request
char		cmd[16], path[64], type[64]; // HTTP-request details
char 		contentLength[16];
std::string header; // HTTP-header
char 		HTTP_OK_HEADER[100] =  "HTTP/1.1 200 OK\r\nServer: BtWifiScanServer/1.1\r\nConnection: close\r\n\r\n";

// JSON parsing
JsonParser 	jsonParser;

// Database
Dao			dao;

// Results
std::vector<Btresult> listBtDevices; // Bluetooth results
std::vector<Wifiresult> listWifiNetworks; // Wifi results

void error(const char *msg) {
    perror(msg);
    system(std::string("echo \"\nError: \nMsg: %s\n \" >> Log.txt", msg).c_str()); // Append log
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
	//if(strcmp(_path, "/clearAll") == 0) return CLEARALL;
	if(strcmp(_path, "/syncBt") == 0) return SYNCBT;
	if(strcmp(_path, "/syncWifi") == 0) return SYNCWIFI;
	if(strcmp(_path, "/upload") == 0) return UPLOAD;
	return 0;
}

int processContentType(char _type[]){
	if(strcmp(_type, "application/json") == 0) return JSONCONTENT;
	if(strcmp(_type, "application/xml") == 0) return XMLCONTENT; // Not tested, XML features not implemented
	return 0;
}

// Send all results in multiple HTTP responses, each containing amount of RESULTRESP results in JSON
// I.e. 35 results, RESULTRESP 8. Send 5 HTTP responses, with results of 8, 8, 8, 8, 3
// Android side uses three spaces to identify each set
void sendResults(vector<Btresult>& _list){
	while(_list.size()!=0){
		vector<Btresult> sendList;
		for(int i=0;i<RESULTRESP;i++){
			if(_list.size()==0) break;
			sendList.push_back(_list.at(0));
			_list.erase(remove(_list.begin(), _list.end(), _list.at(0)), _list.end()); // Remove picked result
		}
		snprintf(outBuff, sizeof(outBuff), "%s   \n", jsonParser.btResultsToJson(sendList).c_str());
		write(connfd, outBuff, strlen(outBuff)); // Send data
		memset(outBuff, sizeof(outBuff), 0);
		sendList.clear();
	}
}

void sendResults(vector<Wifiresult>& _list){
	while(_list.size()!=0){
		vector<Wifiresult> sendList;
		for(int i=0;i<RESULTRESP;i++){
			if(_list.size()==0) break;
			sendList.push_back(_list.at(0));
			_list.erase(remove(_list.begin(), _list.end(), _list.at(0)), _list.end());
		}
		snprintf(outBuff, sizeof(outBuff), "%s   \n", jsonParser.wifiResultsToJson(sendList).c_str());
		write(connfd, outBuff, strlen(outBuff));
		memset(outBuff, sizeof(outBuff), 0);
		sendList.clear();
	}
}

int main(int argc, char **argv) {

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
		snprintf(logBuff, 256, "echo \"\nConnection from: %s:%d \" >> Log.txt", inet_ntop(AF_INET, &cliaddr.sin_addr, clientBuff, sizeof(clientBuff)), ntohs(cliaddr.sin_port));
		system(logBuff); // Append log

		// Read buffer one char at time - stop when reached the start of payload or end of request
		char oneChar[1];
		unsigned int charCounter=0;
		do{
			n = read(connfd, oneChar, 1);
			sscanf(inBuff, "%s %s", cmd, path); // Parse HTTP request's command and path
			inBuff[charCounter] = oneChar[0];
			charCounter++;
			if(strstr(inBuff, "\r\n\r\n")) break; // End of HTTP
			if(strstr(inBuff, "\r\n\n")) break; // Start of payload
		}while(n>0);
		inBuff[charCounter+1] = 0; //Null terminate
		cout << "Read input: ****"<< inBuff << "\n******\n";

		if (n < 0)  // Error with data reading
			snprintf(outBuff, sizeof(outBuff), "Error reading data\n");
		else
			write(connfd, HTTP_OK_HEADER, strlen(HTTP_OK_HEADER)); // Request read successfully

		// Parse Content Type and Length
		std::istringstream req(inBuff);
		while (std::getline(req, header)){
			sscanf(header.c_str(), "Content-Type: %s", type); // HTTP-Header: JSON or XML
			sscanf(header.c_str(), "Content-Length: %s", contentLength);
		}
		// Buffer to hold payload
		char dataBuff[atoi(contentLength)];

		cout << "\n\nRequest details\nCommand: "<< cmd <<"\nContent-Type: "<< type << "\nPath: " << path;
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
						sendResults(listBtDevices);
						snprintf(outBuff, sizeof(outBuff), "\n");
					break;

					case GETALLWIFI:
						listWifiNetworks = dao.getAllWifiResults();
						sendResults(listWifiNetworks);
						snprintf(outBuff, sizeof(outBuff), "\n");
					break;

					case CLEARALL: // Disabled
						dao.tempClearDb();
						snprintf(outBuff, sizeof(outBuff), "DB Cleared.\nCount of Bluetooth Devices: %d\nCount of Wifi Networks: %d\n", dao.getBtCount(), dao.getWifiCount());
					break;
				} // Path
			break;

			case POST:
				switch(processContentType(type)){
					case JSONCONTENT:
						// Read payload to dataBuff
						charCounter = 0;
						do{
							n = read(connfd, oneChar, 1);
							dataBuff[charCounter] = oneChar[0];
							charCounter++;
							if(strstr(dataBuff, "\r\n\r\n")) break;
							if(strstr(dataBuff, "\r\n\n")) break;
							if(charCounter==sizeof(dataBuff)) break;
						}while(n>0);
						dataBuff[charCounter+1] = 0; //Null terminate

						switch(processPath(path)){
							case UPLOAD:
								listBtDevices = jsonParser.parseBtJson(dataBuff, sizeof(dataBuff));
								listWifiNetworks = jsonParser.parseWifiJson(dataBuff, sizeof(dataBuff));
								snprintf(outBuff, sizeof(outBuff), "%d/%lu Bluetooth devices and %d/%lu Wifi networks inserted.\n"
										,dao.insertBtResults(listBtDevices) ,listBtDevices.size(), dao.insertWifiResults(listWifiNetworks), listWifiNetworks.size());
							break;

							case SYNCBT: // TODO: Test same kind of reading as in UPLOAD
								listBtDevices = jsonParser.parseBtJson(dataBuff, sizeof(dataBuff));
								dao.syncBtResults(listBtDevices); // Insert all devices that are not in db already
								sendResults(listBtDevices); // Send response containing devices that were not in user's initial list
								snprintf(outBuff, sizeof(outBuff), "BT sync complete. \n");
							break;

							case SYNCWIFI:
								listWifiNetworks = jsonParser.parseWifiJson(dataBuff, sizeof(dataBuff));
								dao.syncWifiResults(listWifiNetworks);
								sendResults(listWifiNetworks);
								snprintf(outBuff, sizeof(outBuff), "Wifi sync complete. \n");
							break;
						} // Path
					break;

					case XMLCONTENT:
						snprintf(outBuff, sizeof(outBuff), "XML Not supported\n");
					break;
				} // Type
			break;
		}// CMD

		if(outBuff[0]==0)
			snprintf(outBuff, sizeof(outBuff), "Successfully read unknown request\n");

		snprintf(logBuff, 256, "echo \"Command: %s\nPath: %s\nContent-Type: %s\nReply: %s\n\" >> Log.txt",cmd, path, type, outBuff);
		system(logBuff); // Append log

		std::cout << "\nSending: " << outBuff << std::endl; // Print out-going data to server
		write(connfd, outBuff, strlen(outBuff)); // Send data
		close(connfd); //Close current connection, loop back to polling connection

		// Clear everything
		memset(&cmd[0], 0, sizeof(cmd));
		memset(&path[0], 0, sizeof(path));
		memset(&type[0], 0, sizeof(type));
		memset(&inBuff[0], 0, sizeof(inBuff));
		memset(&outBuff[0], 0, sizeof(outBuff));
		memset(&dataBuff[0], 0, sizeof(dataBuff));
		listBtDevices.clear();
		listWifiNetworks.clear();
		req.clear();
	}
}
