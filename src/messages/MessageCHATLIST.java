package messages;

import java.util.*;

import database.*;

// SERVER -> CLIENT
public class MessageCHATLIST extends Message {

	private static final long serialVersionUID = 1L;
	
	private List<ChatHistory> chats;
	
	public MessageCHATLIST(List<ChatHistory> chats) {
		super(MessageType.CHATLIST);
		
		this.chats = chats;
	}
	
	public List<ChatHistory> getChats() {
		return this.chats;
	}
	
}
