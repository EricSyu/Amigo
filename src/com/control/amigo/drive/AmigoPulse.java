package com.control.amigo.drive;


public class AmigoPulse extends Thread{
	private AmigoCommunication Comm;
	
	private boolean active = false;
	
	public AmigoPulse( AmigoCommunication Comm ) {
		// TODO Auto-generated constructor stub
		this.Comm = Comm;
	}
	
	@Override
	public synchronized void run() {
		// TODO Auto-generated method stub
		while( active ){
			try {
				Comm.sendPulse();
				Thread.sleep(1500);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void endPulse(){
		active = false;
	}
	
	public void startPulse(){
		active = true;
		this.start();
	}
	
}
