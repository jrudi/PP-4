package client;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.text.html.*;

import database.*;
import general.Parameters;
import messages.*;

public class UI extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	/** client for communication with the server */
	private Client client;
	
	/** menu items */
	private JMenuItem registrierung;
	private JMenuItem anmeldung;
	private JMenuItem abmeldung;
	private JMenuItem konversation;
	
	/** displays the list of all chat histories as a preview (left) */
	private List<JPanel> preview;
	
	/** stores the index of the currently selected preview */
	private int index = 0;
	
	/** displays the selected chat history in detail (center) */
	private JEditorPane chat;
		
	/** components to send a message */
	private JTextField textfield;
	private JButton send;
	
	/** flag indicating whether the last dialog has been closed by close or by clicking the integrated button
	 * 
	 * <code>true</code>: last dialog has been closed by clicking close
	 * <code>false</code>: last dialog has been closed by clicking the integrated button
	 * */
	private boolean lastJDialogClosed;
	
	public UI(Client client) {
		super("CHAT");

		this.client = client;
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(950, 600);
		this.setLocationRelativeTo(null);
		
		this.addWindowListener(new CloseListener());
		
		this.setJMenuBar(this.menu());
		this.init();
	}
	
	private JMenuBar menu() {
		JMenuBar menu = new JMenuBar();
		
		JMenu verbindungsaufbau = new JMenu("Verbindungsaufbau");
		this.registrierung = new JMenuItem("Registrierung");
		this.registrierung.setActionCommand("REGISTRIERUNG");
		this.registrierung.addActionListener(new ClickListener());
		this.anmeldung = new JMenuItem("Anmeldung");
		this.anmeldung.setActionCommand("ANMELDUNG");
		this.anmeldung.addActionListener(new ClickListener());
		this.abmeldung = new JMenuItem("Abmeldung");
		this.abmeldung.setEnabled(false);
		this.abmeldung.setActionCommand("ABMELDUNG");
		this.abmeldung.addActionListener(new ClickListener());
		verbindungsaufbau.add(this.registrierung);
		verbindungsaufbau.add(this.anmeldung);
		verbindungsaufbau.add(this.abmeldung);
		
		JMenu konversationen = new JMenu("Konversationen");
		this.konversation = new JMenuItem("Konversation starten");
		this.konversation.setEnabled(false);
		this.konversation.setActionCommand("KONVERSATION_STARTEN");
		this.konversation.addActionListener(new ClickListener());
		konversationen.add(this.konversation);
		
		menu.add(verbindungsaufbau);
		menu.add(konversationen);
		
		return menu;
	}
	
	private void init() {
		Container container = this.getContentPane();
		container.setLayout(new BorderLayout(2, 0));
		
		container.add(BorderLayout.WEST, this.left());
		container.add(BorderLayout.CENTER, this.center());
	}
	
	private JScrollPane left() {
		JPanel left = new JPanel();
		left.setLayout(new GridBagLayout());
		left.setOpaque(true);
		left.setBackground(Color.DARK_GRAY);
		
		this.preview = new ArrayList<JPanel>();
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		
		int i = 0;
		
		if(this.client.getData().isEmpty()) {
			left.add(this.space(), constraints);
		} else {
			for(i = 0; i < this.client.getData().size() * 2 - 1; i++) {
				constraints.gridy = i;
					
				if(i % 2 == 0) {
					JPanel preview;
						
					if(this.client.getData().get(i / 2).getMessages().isEmpty()) {
						 preview = this.preview(this.client.getData().get(i / 2).getOther().getUsername(), "", i / 2);
					} else {
						 preview = this.preview(this.client.getData().get(i / 2).getOther().getUsername(), this.client.getData().get(i / 2).getMessages().get(this.client.getData().get(i / 2).getMessages().size() - 1).getMessage(), i / 2);
					}
						
					this.preview.add(preview);
					left.add(preview, constraints);
				} else {
					left.add(this.space(), constraints);
				}
			}
		}
		
		constraints.gridy = i + 1;
		constraints.weighty = 1;
	    left.add(new JLabel(""), constraints);
		
		this.updatePreview();
		
	    JScrollPane pane = new JScrollPane(left);
	    pane.setBorder(null);
	    pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    
		return pane;
	}
	
	private JPanel preview(String username, String message, int index) {
		JPanel preview = new JPanel();
		preview.setPreferredSize(new Dimension(250, 75));
		preview.setLayout(new BorderLayout(10, 10));
		
		JPanel information = new JPanel();
		information.setLayout(new BorderLayout(0, 19));
		information.setOpaque(false);
		
		JLabel otheruser = new JLabel(username);
		otheruser.setFont(otheruser.getFont().deriveFont(Font.BOLD));
		
		JLabel lastmessage = new JLabel(message);
		
		information.add(otheruser, BorderLayout.NORTH);
		information.add(lastmessage, BorderLayout.CENTER);
		
		preview.add(new JLabel(""), BorderLayout.NORTH);
		preview.add(new JLabel(""), BorderLayout.EAST);
		preview.add(new JLabel(""), BorderLayout.WEST);
		preview.add(new JLabel(""), BorderLayout.SOUTH);
		preview.add(information, BorderLayout.CENTER);
		
		preview.addMouseListener(new PreviewListener(index));
		
		return preview;
	}
	
	private JPanel space() {
		JPanel blank = new JPanel();
		
		blank.setPreferredSize(new Dimension(250, 2));
		blank.setOpaque(true);
		blank.setBackground(Color.DARK_GRAY);
		
		return blank;
	}
	
	private JPanel center() {
		JPanel center = new JPanel();
		center.setLayout(new BorderLayout());
		center.setOpaque(true);
		center.setBackground(Color.LIGHT_GRAY);
		
		center.add(this.chat(), BorderLayout.CENTER);
		if(!this.client.getData().isEmpty()) {
			center.add(this.input(), BorderLayout.SOUTH);
		}

		return center;
	}
	
	private JScrollPane chat() {		
        this.chat = new JEditorPane();
        this.chat.setEditable(false);
        this.chat.setContentType("text/html");
        Font font = UIManager.getFont("Label.font");
        String rule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; margin: 0px 10px 10px 10px;}";
        ((HTMLDocument) this.chat.getDocument()).getStyleSheet().addRule(rule);
        
        this.updateChat();

	    JScrollPane pane = new JScrollPane(this.chat);
	    pane.setBorder(null);
	    pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    pane.getVerticalScrollBar().setValue(0);
	    
		return pane;
	}
	
	private JPanel input() {
		JPanel input = new JPanel();
		input.setLayout(new BorderLayout(10, 10));
		
		JPanel content = new JPanel();
		content.setLayout(new BorderLayout(10, 0));
		this.textfield = new JTextField("");
		content.add(BorderLayout.CENTER, this.textfield);
		this.send = new JButton("Senden");
		this.send.setActionCommand("SENDEN");
		this.send.addActionListener(new ClickListener());
		content.add(BorderLayout.EAST, this.send);
		
		input.add(new JLabel(""), BorderLayout.NORTH);
		input.add(new JLabel(""), BorderLayout.EAST);
		input.add(new JLabel(""), BorderLayout.WEST);
		input.add(new JLabel(""), BorderLayout.SOUTH);
		input.add(BorderLayout.CENTER, content);
		
		return input;
	}
	
	private void updatePreview() {
		for(JPanel panel : preview) {
			panel.setBackground(Color.WHITE);
		}
		
		if(!this.preview.isEmpty()) {
			this.preview.get(this.index).setBackground(Color.CYAN);
		}
	}
	
	private void updateChat() {
		if(!this.client.getData().isEmpty()) {
			ChatHistory history = this.client.getData().get(this.index);
			List<ChatMessage> messages = history.getMessages();
				
			StringBuffer text = new StringBuffer();
		    for(int i = 0; i < messages.size(); i++) {
		        if(messages.get(i).getFrom().getUsername().equals(history.getOther().getUsername())) {
		        	text.append("<p style=\"text-align: left;\">");
		        } else {
		        	text.append("<p style=\"text-align: right;\">");
		        }
		        	
		        text.append(messages.get(i).getMessage());
		    	text.append("</p>");
		    }
		        
		    this.chat.setText(text.toString());
		} else {
			if(UI.this.client.getUsername().equals("")) {
				this.chat.setText("<p style=\"text-align: center; font-size: 29px;\">BITTE ANMELDEN!</p>");
			} else {
				this.chat.setText("<p style=\"text-align: center; font-size: 29px;\">BISHER KEIN CHATVERLAUF!</p>");
			}
		}
	}
	
	private String displayDialogUsers(List<ChatUser> users) {
		UI.this.lastJDialogClosed = false;
		
		this.setEnabled(false);
		
        JDialog dialog = new JDialog(null, "Benutzerliste", ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(350, 150);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);
 
        List<JRadioButton> buttons = new ArrayList<JRadioButton>();
        for(ChatUser user : users) {
        	boolean placement = true;
        
        	for(ChatHistory history : this.client.getData()) {
            	if(history.getOther().equals(user)) {
            		placement = false;
            	}
            }
        	
        	if(placement) {
        		JRadioButton button = new JRadioButton(user.toString());
        		button.setActionCommand(user.toString());
        		
        		buttons.add(button);
        	}
        }
        
        JPanel userpanel = new JPanel();
        ButtonGroup group = null;
        if(buttons.isEmpty()) {
        	userpanel.setLayout(new GridLayout(1, 1, 0, 10));
        	
        	userpanel.add(new JLabel("<html><br>KEINE WEITEREN BENUTZER</html>"));
        } else {
        	userpanel.setLayout(new GridLayout(buttons.size(), 1, 0, 0));
        	
        	group = new ButtonGroup();
        	for(JRadioButton button : buttons) {
        		group.add(button);
        		userpanel.add(button);
        	}
        }
        
        JPanel actionpanel = new JPanel();
        actionpanel.setLayout(new BorderLayout());
        JButton action = new JButton("Konversation starten");
        action.addActionListener(new DialogClickListener(dialog));
        actionpanel.add(new JLabel(""), BorderLayout.CENTER);
        actionpanel.add(action, BorderLayout.EAST);
        
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout(10, 10));
        JScrollPane userpane = new JScrollPane(userpanel);
        userpane.setBorder(null);
        userpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        userpane.getVerticalScrollBar().setValue(0);
	    content.add(userpane, BorderLayout.CENTER);
	    content.add(actionpanel, BorderLayout.SOUTH);

	    dialog.setLayout(new BorderLayout(10, 10));
        dialog.add(new JLabel(""), BorderLayout.NORTH);
        dialog.add(new JLabel(""), BorderLayout.EAST);
        dialog.add(new JLabel(""), BorderLayout.WEST);
        dialog.add(new JLabel(""), BorderLayout.SOUTH);
        dialog.add(content, BorderLayout.CENTER);
        
        dialog.addWindowListener(new DialogCloseListener());
        dialog.setVisible(true);
        
        // execution continues only after dialog.dispose() has been called
        
        this.setEnabled(true);
        
        if(!UI.this.lastJDialogClosed && group != null && group.getSelection() != null) {
        	return group.getSelection().getActionCommand();
        } else {
        	return "";
        }
    }
	
	private class PreviewListener extends MouseAdapter {
		
		private int index;
		
		PreviewListener(int index) {
			this.index = index;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			UI.this.index = this.index;
			UI.this.textfield.setText("");
			UI.this.updatePreview();
			UI.this.updateChat();
		}
		
	}
	
	private class DialogClickListener implements ActionListener {

		private JDialog dialog;
		
		DialogClickListener(JDialog dialog) {
			this.dialog = dialog;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			this.dialog.dispose();
		}
		
	}
	
	private class DialogCloseListener extends WindowAdapter {
		
		@Override
		public void windowClosing(WindowEvent e) {
			UI.this.lastJDialogClosed = true;
		}
		
	}
	
	/* RELEVANT METHODS */
	
	/**
	 * changes the enabling of the menu items so that a login becomes possible
	 */
	public void enableMenuLOGIN() {
		this.registrierung.setEnabled(true);
		this.anmeldung.setEnabled(true);
		this.abmeldung.setEnabled(false);
		this.konversation.setEnabled(false);
	}
	
	/**
	 * changes the enabling of the menu items so that a logout becomes possible
	 */
	public void enableMenuLOGOUT() {
		this.registrierung.setEnabled(false);
		this.anmeldung.setEnabled(false);
		this.abmeldung.setEnabled(true);
		this.konversation.setEnabled(true);
	}
	
	/**
	 * updates all relevant components of the user interface by keeping as much information as possible
	 */
	public void updateUI() {
		String currentMessage = "";
		if(this.textfield != null) {
			currentMessage = this.textfield.getText();
		}
		
		ChatUser currentOther = null;
		if(this.client.getData() != null && !this.client.getData().isEmpty()) {
			currentOther = this.client.getData().get(this.index).getOther();
		}
			
		this.getContentPane().removeAll();
		Collections.sort(this.client.getData());
			
		if(currentOther != null) {
			for(int i = 0; i < this.client.getData().size(); i++) {
				if(this.client.getData().get(i).getOther().equals(currentOther)) {
					this.index = i;
				}
			}
		}
		
		this.init();
		
		if(this.textfield != null) {
			this.textfield.setText(currentMessage);
		}
		
		this.repaint();
		this.revalidate();
		this.setVisible(true);
	}
	
	/**
	 * plays a sound that can be played after sending a message
	 */
	public void playSEND() {
		try {
			Clip clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(new File(Parameters.SOUND_SEND)));
		    clip.start();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * plays a sound that can be played after receiving a message
	 */
	public void playUPDATE() {
		try {
			Clip clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(new File(Parameters.SOUND_RECEIVE)));
		    clip.start();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * creates a new dialog with the specified text for title, message and button and returns the string the user entered in the integrated text field
	 * 
	 * @param title text that appears as the title of this dialog frame
	 * @param message text that appears as an information for the user
	 * @param button text that appears on the integrated button
	 * @return string the user entered in the integrated text field or null if the dialog has been closed by the user
	 */
	public String showDialogGENERIC(String title, String message, String button) {		
		UI.this.lastJDialogClosed = false;
		
		this.setEnabled(false);
		
		JDialog dialog = new JDialog(null, title, ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(350, 150);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);
 
        JPanel content = new JPanel();
        content.setLayout(new GridLayout(3, 1, 0, 10));
        
        JLabel label = new JLabel("<html><br>" + message + "</html>");
        JTextField input = new JTextField();
        JPanel actionpanel = new JPanel();
        actionpanel.setLayout(new BorderLayout());
        JButton action = new JButton(button);
        action.addActionListener(new DialogClickListener(dialog));
        actionpanel.add(new JLabel(""), BorderLayout.CENTER);
        actionpanel.add(action, BorderLayout.EAST);
        
        content.add(label);
        content.add(input);
        content.add(actionpanel);
        
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.add(new JLabel(""), BorderLayout.NORTH);
        dialog.add(new JLabel(""), BorderLayout.EAST);
        dialog.add(new JLabel(""), BorderLayout.WEST);
        dialog.add(new JLabel(""), BorderLayout.SOUTH);
        dialog.add(content, BorderLayout.CENTER);
 
        dialog.addWindowListener(new DialogCloseListener());
        dialog.setVisible(true);
 
        // execution continues only after dialog.dispose() has been called
        
        this.setEnabled(true);
        
        if(this.lastJDialogClosed) {
        	return null;
        } else {
        	return input.getText();
        }
	}
	
	/**
	 * creates a new dialog with the specified list of users and opens a new chat with the user that has been chosen
	 * 
	 * @param users list of users to choose from
	 */
	public void showDialogUSERLIST(List<ChatUser> users) {
		String result = this.displayDialogUsers(users);
		
		if(!result.equals("")) {
			this.client.addChatHistoryNew(0, new ChatHistory(new ChatUser(result), new ArrayList<ChatMessage>()));
			this.index = 0;
			this.updateUI();
		}
	}
	
	/**
	 * creates a new dialog with the specified error message
	 * 
	 * @param error error message to display
	 */
	public void showDialogERROR(String error) {
		JOptionPane.showMessageDialog(null, error, "ERROR", JOptionPane.ERROR_MESSAGE);
	}
	
	private class ClickListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch(e.getActionCommand()) {
				case "REGISTRIERUNG":
					/* YOUR IMPLEMENTATION */
					
					break;
				case "ANMELDUNG":
					/* YOUR IMPLEMENTATION */
					
					break;
				case "ABMELDUNG":
					/* YOUR IMPLEMENTATION */

					break;
				case "KONVERSATION_STARTEN":
					/* YOUR IMPLEMENTATION */
					
					break;
				case "SENDEN":
					/* YOUR IMPLEMENTATION */

					break;
				default:
					break;
			}
			
		}
		
	}
	
	private class CloseListener extends WindowAdapter {
		
		@Override
		public void windowClosing(WindowEvent e) {
			/* YOUR IMPLEMENTATION */
		}
		
	}
	
}
