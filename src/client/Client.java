package client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import database.*;
import general.*;
import messages.*;

public class Client {
	
	/** connections */
	private Socket socket;
	private ObjectOutputStream output;
    private ObjectInputStream input;
    
    /** user interface */
    private UI ui;
    
    /** thread which is permanently requesting for new data at the server */
    private PullThread thread;
    
    /** user that is currently logged in for this client ("" means that currently no user is logged in for this client) */
    private String username;
    
    /** chat data this client currently knows */
    private List<ChatHistory> data;
	
	public Client() {
		this.data = new ArrayList<ChatHistory>();
		this.username = "";
	}
	
	private void initialize() {
		try {
			this.socket = new Socket(Parameters.HOST, Parameters.PORT_CLIENT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.connect();
		
		this.ui = new UI(this);
		this.ui.setVisible(true);
	}
	
	private void connect() {
		try {
			this.output = new ObjectOutputStream(socket.getOutputStream());
			this.input = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.thread = new PullThread(this);
		this.thread.start();
	}
	
	public void addChatHistoryNew(int index, ChatHistory history) {
		this.data.add(index, history);
	}
	
	/* RELEVANT METHODS */
	
	/** 
	 * closes all connections with the server and terminates the pull thread
	 */
	public void disconnect() {
		this.thread.end();
		
		try {
			this.input.close();
			this.output.close();
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	/**
	 * sets the name of the user that is currently logged in for this client to the specified parameter
	 * 
	 * @param username name of the user
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * returns the name of the user that is currently logged in for this client
	 * 
	 * @return name of the user that is currently logged in for this client
	 */
	public String getUsername() {
		return this.username;
	}
	
	/**
	 * sets the chat data this client currently knows to the specified parameter
	 * 
	 * @param newData chat data this client currently knows
	 */
	public void setData(List<ChatHistory> newData) {
		this.data = newData;
	}
	
	/**
	 * returns the chat data this client currently knows
	 * 
	 * @return chat data this client currently knows
	 */
	public List<ChatHistory> getData() {
    	return this.data;
    }

	/**
	 * adds the specified chat message to an existing chat history in {@link Client#data} at the specified index
	 * 
	 * @param index index of the chat history in the currently stored chat data where the new chat message should be added
	 * @param message chat message to add to an existing chat history in the currently stored chat data
	 */
	public void addChatMessageSend(int index, ChatMessage message) {
		this.data.get(index).getMessages().add(message);
	}
	
	/**
	 * integrates the specified chat history with unread chat messages to an existing chat history in {@link Client#data}
	 * 
	 * @param newData chat history with unread chat messages to integrate in an existing chat history in the currently stored chat data
	 */
	public void addChatHistoryUnread(List<ChatHistory> newData) {
		ChatHistory[] copy = new ChatHistory[newData.size()];
		newData.toArray(copy); // copy to avoid concurrent modification exception
			
		for(int i = 0; i < this.data.size(); i++) {
			for(ChatHistory history : copy) {
				if(history.getOther().equals(this.data.get(i).getOther())) {
					this.data.get(i).getMessages().addAll(history.getMessages());
					newData.remove(history);
				}
			}
		}
		
		if(!newData.isEmpty()) {
			this.data.addAll(newData);
		}
	}
	
	public void send(Message m){
		try {
			System.out.println("S: " + m.getClass().getName());
			output.writeObject(m);
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Message receive(){
		try {
			Message m = (Message)input.readObject();
			System.out.println("R: " + m.getClass().getName());
			return m;
			
		}catch(IOException | ClassNotFoundException ie){
			ie.printStackTrace();
			return null;
		}
	}
	
	public void handleUPDATE(MessageCHATLIST_UPDATE mcu){
		if (mcu.getChats().size()>0){
			addChatHistoryUnread(mcu.getChats());
			ui.updateUI();
			ui.playUPDATE();
		}	
	}
	
	public void sendChatMessage(int index, String message){
		ChatUser self = new ChatUser(username);
		ChatUser other = this.getData().get(index).getOther();
		ChatMessage cm = new ChatMessage(self, other, message, System.currentTimeMillis());
		addChatMessageSend(index, cm);
		send(new MessageSEND(cm));
		ui.updateUI();
	}
	

	public static void main(String[] args) {
		Client client = new Client();
		client.initialize();
	}
	
}
