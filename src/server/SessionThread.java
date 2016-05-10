package server;

import java.io.*;
import java.net.*;
import java.util.*;

import database.*;
import messages.*;

public class SessionThread extends Thread {
	
	/** connections */
	private Socket socket;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	
	/** database */
	private ADatabaseHANDLE database;
	
	/** user that is currently logged in for this session ("" means that currently no user is logged in for this session) */
	private String username;
	
	/** flag for controlling the loop of this thread */
	private boolean running;
	
	SessionThread(Socket socket, ADatabaseHANDLE database) {
		this.socket = socket;
		this.database = database;
		
		this.username = "";
		this.running = true;
		
		this.connect();
	}
	
	private void connect() {
		try {
			this.input = new ObjectInputStream(this.socket.getInputStream());
			this.output = new ObjectOutputStream(this.socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* RELEVANT METHODS */
	
	/** 
	 * closes all connections with the client
	 */
	private void disconnect() {
		try {
			this.output.close();
			this.input.close();
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/* YOUR IMPLEMENTATION */

}
