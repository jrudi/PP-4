package messages;

import java.util.*;

import database.*;

// SERVER -> CLIENT
public class MessageUSERLIST extends Message {
	
	private static final long serialVersionUID = 1L;
	
	private List<ChatUser> users;
	
	public MessageUSERLIST(List<ChatUser> users) {
		super(MessageType.USERLIST);
		
		this.users = users;
	}
	
	public List<ChatUser> getUsers() {
		return this.users;
	}

}
