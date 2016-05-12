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
	
	private void send(Message m){
		try {
			output.writeObject(m);
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run(){
		while(running){
			try {
				Message m = (Message)input.readObject();
				
				switch(m.getType()){
				case QUIT:
					disconnect();
					this.running = false;
					break;
				case LOGIN:
					MessageLOGIN ml= (MessageLOGIN)m;
					if(database.existsChatUser(ml.getUser())){
						this.username = ml.getUser().getUsername();
						database.updateUnreadChatMessages(ml.getUser());
						MessageCHATLIST mu = new MessageCHATLIST(database.selectChatHistory(ml.getUser()));
						send(mu);
					}else{
						MessageERROR_LOGIN mel = new MessageERROR_LOGIN("Der Benutzer ist nicht registriert!");
						send(mel);
					}
					break;
				case REGISTER:
					MessageREGISTER mr= (MessageREGISTER)m;
					if(database.insertChatUser(mr.getUser())){
						this.username = mr.getUser().getUsername();
						MessageCHATLIST mu = new MessageCHATLIST(database.selectChatHistory(mr.getUser()));
						send(mu);

					}else{
						MessageERROR_REGISTER mer = new MessageERROR_REGISTER("Der Benutzername ist bereits vergeben!");
						send(mer);
					}
					break;
				case UPDATE:
					if(this.username.equals("")){
						send(new MessageCHATLIST_UPDATE(new ArrayList<ChatHistory>()));
					}else{
						ChatUser cu = new ChatUser(username);
						send(new MessageCHATLIST_UPDATE(database.selectUnreadChatHistory(cu)));
						database.updateUnreadChatMessages(cu);
					}
					break;
				case USERS:
						send(new MessageUSERLIST(database.selectChatUsers()));
					break;
				case SEND:
					MessageSEND cm = (MessageSEND)m;
					database.insertChatMessage(cm.getMessage());
					ChatUser cu = new ChatUser(username);
					send(new MessageCHATLIST_UPDATE(database.selectUnreadChatHistory(cu)));
					database.updateUnreadChatMessages(cu);
					break;
				case LOGOUT:
					username = "";
					send(new MessageCHATLIST(new ArrayList<ChatHistory>()));
					break;
				default:
					break;
				
				}
				
				
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}
	}
}
