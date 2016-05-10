package database;

import java.sql.*;
import java.util.*;

public class DatabaseHANDLE extends ADatabaseHANDLE {
	
	private PreparedStatement usersINSERT;
	private PreparedStatement messagesINSERT;
	
	private PreparedStatement messagesunreadUPDATE;

	private PreparedStatement usernameSELECT;
	private PreparedStatement usersSELECT;
	private PreparedStatement messagesSELECT;
	private PreparedStatement messagesunreadSELECT;
	
	public DatabaseHANDLE(String file) {		
		super(file);
		
		this.prepare();
	}
	
	private void prepare() {
		try {
			// registration of a new user
			this.usersINSERT = this.connection.prepareStatement("INSERT INTO users(username) VALUES(?);");
			
			// insertion of a new chat message
			this.messagesINSERT = this.connection.prepareStatement("INSERT INTO messages(userfrom, userto, message, time, unread) VALUES(?, ?, ?, ?, ?);");
		
			// mark all messages as read for a specified user
			this.messagesunreadUPDATE = this.connection.prepareStatement("UPDATE messages SET unread=0 WHERE userto=? AND unread=1;");
			
			// check whether a user exists in the database or not
			this.usernameSELECT = this.connection.prepareStatement("SELECT username FROM users WHERE username=?;");
			
			// select all users
			this.usersSELECT = this.connection.prepareStatement("SELECT username FROM users;");
			
			// select all messages between two users (both directions)
			this.messagesSELECT = this.connection.prepareStatement("SELECT userfrom, userto, message, time FROM messages WHERE userfrom=? AND userto=? OR userfrom=? AND userto=?;");
			
			// select all unread messages for one user (not both directions)
			this.messagesunreadSELECT = this.connection.prepareStatement("SELECT userfrom, userto, message, time FROM messages WHERE userfrom=? AND userto=? AND unread=1;");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
		
	public synchronized boolean insertChatUser(ChatUser user) {
		if(this.existsChatUser(user) || user.getUsername().equals("")) {
			return false;
		} else {
			try {
				this.usersINSERT.setString(1, user.getUsername());
				
				this.usersINSERT.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return true;
		}
	}
	
	public synchronized boolean insertChatMessage(ChatMessage message) {
		if(!this.existsChatUser(message.getFrom()) || !this.existsChatUser(message.getTo())) {
			return false;
		}
		
		try {			
			this.messagesINSERT.setString(1, message.getFrom().getUsername());
			this.messagesINSERT.setString(2, message.getTo().getUsername());
			this.messagesINSERT.setString(3, message.getMessage());
			this.messagesINSERT.setLong(4, message.getTime());
			if(message.getFrom().equals(message.getTo())) {
				this.messagesINSERT.setInt(5, 0);
			} else {
				this.messagesINSERT.setInt(5, 1);
			}
			
			this.messagesINSERT.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return true;
	}
	
	public synchronized boolean updateUnreadChatMessages(ChatUser userto) {
		if(!this.existsChatUser(userto)) {
			return false;
		} else {
			try {
				this.messagesunreadUPDATE.setString(1, userto.getUsername());
				
				this.messagesunreadUPDATE.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return true;
		}
	}
	
	public synchronized boolean existsChatUser(ChatUser user) {
		try {
			this.usernameSELECT.setString(1, user.getUsername());
			
			ResultSet rs = this.usernameSELECT.executeQuery();
			while(rs.next()) {
				if(user.getUsername().equals(rs.getString(1))) {
					return true;
				} else {
					return false;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
			
		return false;
	}
	
	public synchronized List<ChatUser> selectChatUsers() {		
		List<ChatUser> users = new ArrayList<ChatUser>();
		
		try {
			ResultSet rs = this.usersSELECT.executeQuery();
			while(rs.next()) {
				users.add(new ChatUser(rs.getString(1)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return users;
	}
	
	private List<ChatMessage> selectChatMessages(ChatUser userfrom, ChatUser userto) {
		List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
		
		try {
			this.messagesSELECT.setString(1, userfrom.getUsername());
			this.messagesSELECT.setString(2, userto.getUsername());
			this.messagesSELECT.setString(3, userto.getUsername());
			this.messagesSELECT.setString(4, userfrom.getUsername());
			
			ResultSet rs = this.messagesSELECT.executeQuery();
			while(rs.next()) {
				chatMessages.add(new ChatMessage(new ChatUser(rs.getString(1)), new ChatUser(rs.getString(2)), rs.getString(3), rs.getLong(4)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return chatMessages;
	}
	
	public synchronized List<ChatHistory> selectChatHistory(ChatUser userfrom) {
		List<ChatHistory> chats = new ArrayList<ChatHistory>();
		
		List<ChatUser> users = this.selectChatUsers();
		for(ChatUser userto : users) {
			List<ChatMessage> chat = this.selectChatMessages(userfrom, userto);
			
			if(!chat.isEmpty()) {
				chats.add(new ChatHistory(userto, chat));
			}
		}
		
		Collections.sort(chats);
		return chats;
	}
	
	private List<ChatMessage> selectUnreadChatMessages(ChatUser userfrom, ChatUser userto) {
		List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
		
		try {
			this.messagesunreadSELECT.setString(1, userfrom.getUsername());
			this.messagesunreadSELECT.setString(2, userto.getUsername());
			
			ResultSet rs = this.messagesunreadSELECT.executeQuery();
			while(rs.next()) {
				chatMessages.add(new ChatMessage(new ChatUser(rs.getString(1)), new ChatUser(rs.getString(2)), rs.getString(3), rs.getLong(4)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return chatMessages;
	}
	
	public synchronized List<ChatHistory> selectUnreadChatHistory(ChatUser userto) {
		List<ChatHistory> chats = new ArrayList<ChatHistory>();
		
		List<ChatUser> users = this.selectChatUsers();
		for(ChatUser userfrom : users) {
			List<ChatMessage> chat = this.selectUnreadChatMessages(userfrom, userto);
			
			if(!chat.isEmpty()) {
				chats.add(new ChatHistory(userfrom, chat));
			}
		}
		
		Collections.sort(chats);
		return chats;
	}
	
}
