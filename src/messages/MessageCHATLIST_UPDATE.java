package messages;

import java.util.*;

import database.*;

// SERVER -> CLIENT
public class MessageCHATLIST_UPDATE extends Message {

	private static final long serialVersionUID = 1L;
	
	private List<ChatHistory> chats;
	
	public MessageCHATLIST_UPDATE(List<ChatHistory> chats) {
		super(MessageType.CHATLIST_UPDATE);
		
		this.chats = chats;
	}
	
	public List<ChatHistory> getChats() {
		return this.chats;
	}
	
}
