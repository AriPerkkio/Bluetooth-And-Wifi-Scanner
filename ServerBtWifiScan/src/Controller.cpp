/* A simple server in the internet domain using TCP
   The port number is passed as an argument */
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

void error(const char *msg) {
    perror(msg);
    exit(1);
}

int main(int argc, char **argv) {

	// Connection
	int 		listenfd, connfd, n; // Socket IDs; Listening socket and connected socket. n for indexing loops
	struct 		sockaddr_in servaddr, cliaddr; // Address structure to hold server's and client's address
	char 		inBuff[2048], outBuff[2048], logBuff[2048]; // Data buffers for input and output + logs
	char 		clientBuff[256]; // To hold converted client address
	socklen_t 	len; // Client address info

	// HTTP Request
	char		cmd[16], type[64]; // HTTP-request details
	std::string header; // HTTP-header

	// JSON parsing
	char		btInitialJsonParse[] = "{\"btscans\":[%[^]]%n", wifiInitialJsonParse[] = "{\"wifiscans\":[%[^]]";
	char		btJsonParse[] = "\"devName\":\"%[^\"]\", \"devAddr\":\"%[^\"]\", \"devType\":\"%[^\"]\", \"devRssi\":\"%[^\"]\", \"location\":\"%[^\"]\"";
	char		wifiJsonParse[] = "\"ssid\":\"%[^\"]\", \"bssid\":\"%[^\"]\", \"capabilities\":\"%[^\"]\", \"rssi\":\"%[^\"]\", \"freq\":\"%[^\"]\", \"location\":\"%[^\"]\"";
	char		btjson[2048], btdevjson[1024], devName[64], devAddr[64], devType[64], devRssi[64], btloc[64];
	char		wifijson[2048], wifinetjson[1024], ssid[64], bssid[64], capabilities[64], wifiRssi[64], freq[64], wifiloc[64];

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
			sscanf(inBuff, "%s", cmd); // Parse HTTP request's command
			inBuff[n] = 0; //Null terminate
			if(strstr(inBuff, "\r\n\r\n")) break; // End of HTTP-request
		}

		// Parse HTTP headers
		std::istringstream req(inBuff);
		while (std::getline(req, header)) {
			int readChars = 0;
			sscanf(header.c_str(), "Content-Type: %s", type); // HTTP-Header: JSON or XML
			sscanf(header.c_str(), btInitialJsonParse, btjson, &readChars); // JSON for all bt results
			sscanf(header.c_str()+readChars+2, wifiInitialJsonParse, wifijson); // JSON for all wifi results
		}
		// Parse btJson into bt results
		int offset = 0, pos;
		while(sscanf(btjson+offset, "{%[^}]%n", btdevjson, &pos)!=0) {
			if(btdevjson[0]==0) break; // TODO: Better way for checking empty
			offset += pos+2;
			if(sscanf(btdevjson, btJsonParse, devName, devAddr, devType, devRssi, btloc) == 5) // All five successful
				listBtDevices.push_back(Btresult(devName, devAddr, devType, devRssi, btloc)); // Add new bt result
			memset(&btdevjson[0], 0, sizeof(btdevjson));
		}
		// Parse WifiJson into wifi results
		offset = 0;
		while(sscanf(wifijson+offset, "{%[^}]%n", wifinetjson, &pos)!=0){
			if(wifinetjson[0]==0) break; // TODO: Better way for checking empty
			offset += pos+2;
			if(sscanf(wifinetjson, wifiJsonParse, ssid, bssid, capabilities, wifiRssi, freq, wifiloc) == 6) // All six successful
				listWifiNetworks.push_back(Wifiresult(ssid, bssid, capabilities, wifiRssi, freq, wifiloc)); // Add wifi result
			memset(&wifinetjson[0], 0, sizeof(wifinetjson));
		}

		printf("\n\nRequest details \nCommand: %s\nContent-Type: %s\n", cmd, type);
		snprintf(logBuff, 256, "echo \"Command: %s\nContent-Type: %s\" >> Log.txt",cmd, type);
		system(logBuff); // Append log

		for(unsigned int i=0;i<listBtDevices.size();i++)
			listBtDevices.at(i).printInfo();

		for(unsigned int i=0;i<listWifiNetworks.size();i++)
			listWifiNetworks.at(i).printInfo();

		if (n < 0)  // Error with data reading
			snprintf(outBuff, sizeof(outBuff), "Error reading data\n");
		else
			snprintf(outBuff, sizeof(outBuff), "Successfully read request\n");

		printf("\nSending: %s\n", outBuff ); // Print out-going data to server
		write(connfd, outBuff, strlen(outBuff)); //wire data to the client
		close(connfd); //Close current connection, loop back to polling connection

		// Clear everything
		memset(&type[0], 0, sizeof(type));
		memset(&btjson[0], 0, sizeof(btjson));
		memset(&btdevjson[0], 0, sizeof(btdevjson));
		memset(&wifijson[0], 0, sizeof(wifijson));
		memset(&wifinetjson[0], 0, sizeof(wifinetjson));
		memset(&inBuff[0], 0, sizeof(inBuff));
		memset(&outBuff[0], 0, sizeof(outBuff));
		listBtDevices.clear();
		listWifiNetworks.clear();
	}
}

/**
curl -H "Content-Type: application/json" -X POST -d @file.json http://localhost:8888
curl -H "Content-Type: application/json" -X POST -d '{"username":"xyz"}\n{"password":"xyz"}' http://localhost:8888
curl -H "Content-Type: application/json" -X POST -d '{"username":"xyz","password":"xyz"}' http://localhost:8888
 **/
