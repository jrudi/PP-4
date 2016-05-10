package messages;

// CLIENT -> SERVER
public class MessageUSERS extends Message {

	private static final long serialVersionUID = 1L;
	
	public MessageUSERS() {
		super(MessageType.USERS);
	}
	
}
