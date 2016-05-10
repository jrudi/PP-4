package messages;

// CLIENT -> SERVER
public class MessageUPDATE extends Message {

	private static final long serialVersionUID = 1L;
	
	public MessageUPDATE() {
		super(MessageType.UPDATE);
	}
	
}
