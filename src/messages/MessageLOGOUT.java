package messages;

// CLIENT -> SERVER
public class MessageLOGOUT extends Message {

	private static final long serialVersionUID = 1L;
	
	public MessageLOGOUT() {
		super(MessageType.LOGOUT);
	}
	
}
