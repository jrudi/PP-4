package messages;

// SERVER -> CLIENT
public class MessageERROR_REGISTER extends Message {

	private static final long serialVersionUID = 1L;

	private String error;
	
	public MessageERROR_REGISTER(String error) {
		super(MessageType.ERROR_REGISTER);
		
		this.error = error;
	}

	public String getError() {
		return this.error;
	}
	
}
