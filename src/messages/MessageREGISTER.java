package messages;

import database.*;

// CLIENT -> SERVER
public class MessageREGISTER extends Message {
	
	private static final long serialVersionUID = 1L;

	private ChatUser user;
	
	public MessageREGISTER(ChatUser user) {
		super(MessageType.REGISTER);
		
		this.user = user;
	}
	
	public ChatUser getUser() {
		return this.user;
	}
	
}
