package server;

import java.io.*;
import java.net.*;

import database.*;
import general.*;

public class Server {
	
	private ServerSocket server;
	private ADatabaseHANDLE database;
	
	public Server() {
		try {
			this.server = new ServerSocket(Parameters.PORT_SERVER);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.database = new DatabaseHANDLE(Parameters.DATABASE);
	}
	
	public void listen() {
		try {
			while(true) {
				Socket client = this.server.accept();

				SessionThread session = new SessionThread(client, this.database);
				session.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
        Server server = new Server();
        server.listen();
    }
	
}
