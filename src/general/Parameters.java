package general;

public class Parameters {

	public static final String HOST = "localhost";
	
	public static final int PORT_SERVER = 12345;
	public static final int PORT_CLIENT = Parameters.PORT_SERVER;
	
	public static final String DATABASE = "src" + System.getProperty("file.separator") + "resources" + System.getProperty("file.separator") + "chat.db";
	
	public static final String SOUND_SEND = "src" + System.getProperty("file.separator") + "resources" + System.getProperty("file.separator") + "notification_send.wav";
	public static final String SOUND_RECEIVE = "src" + System.getProperty("file.separator") + "resources" + System.getProperty("file.separator") + "notification_receive.wav";
	
}
