package messages;

import database.*;

// CLIENT -> SERVER
public class MessageSEND extends Message {

	private static final long serialVersionUID = 1L;
	
	private ChatMessage message;
	
	public MessageSEND(ChatMessage message) {
		super(MessageType.SEND);
		
		this.message = message;
	}
	
	public ChatMessage getMessage() {
		return this.message;
	}
	
}
