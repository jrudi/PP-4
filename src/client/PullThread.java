package client;

import messages.*;

public class PullThread extends Thread{

	private Client client;
	private boolean ended;
	
	public PullThread(Client c){
		client = c;
	}
	public void end(){
		ended = true;
	}
	
	public void run(){
		while(!ended)
		try {
			client.send(new MessageUPDATE());
			MessageCHATLIST_UPDATE mcu = (MessageCHATLIST_UPDATE)client.receive();
			client.handleUPDATE(mcu);
			Thread.sleep(1000);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
