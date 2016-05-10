package messages;

// SERVER -> CLIENT
public class MessageERROR_LOGIN extends Message {

	private static final long serialVersionUID = 1L;

	private String error;
	
	public MessageERROR_LOGIN(String error) {
		super(MessageType.ERROR_LOGIN);
		
		this.error = error;
	}

	public String getError() {
		return this.error;
	}
	
}
