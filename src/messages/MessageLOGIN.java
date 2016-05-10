package messages;

import database.*;

// CLIENT -> SERVER
public class MessageLOGIN extends Message {
	
	private static final long serialVersionUID = 1L;

	private ChatUser user;
	
	public MessageLOGIN(ChatUser user) {
		super(MessageType.LOGIN);
		
		this.user = user;
	}
	
	public ChatUser getUser() {
		return this.user;
	}

}
