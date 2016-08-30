package com.control.amigo.drive;

public class WanderMode implements Runnable {
	private AmigoCommunication Comm;
	
	private static boolean active = false;
	private int[] sonar;
	
	private int transV = 400, rotV = 40, stall = 1;
	private boolean forward = true;
	
	public WanderMode(AmigoCommunication Comm ) {
		// TODO Auto-generated constructor stub
		this.Comm = Comm;
		sonar = new int[8];
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			Comm.changeSonarCycle(10);
			Comm.setMaxTransVelocity(transV);
			Comm.setMaxRotVelocity(rotV);
			Thread.sleep(1000);
			sonar = PacketReceiver.mAmigoInfo.getSonars();
			stall = PacketReceiver.mAmigoInfo.getstall();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while( active ){
			try {
				while( sonar[2]<600 && stall==0 && active ){
					Comm.setTransVelocity(0);
					Comm.setRotVelocity((-1)*rotV);
					Thread.sleep(randomSleeptime());
					sonar = PacketReceiver.mAmigoInfo.getSonars();
					stall = PacketReceiver.mAmigoInfo.getstall();
					forward = true;
					
					chectStall();
				}
			
				while( sonar[3]<600 && stall==0 && active ){
					Comm.setTransVelocity(0);
					Comm.setRotVelocity(rotV);
					Thread.sleep(randomSleeptime());
					sonar = PacketReceiver.mAmigoInfo.getSonars();
					stall = PacketReceiver.mAmigoInfo.getstall();
					forward = true;
					
					chectStall();
				}
				
				while( sonar[1]<550 && stall==0 && active ){
					Comm.setTransVelocity(0);
					Comm.setRotVelocity((-1)*rotV);
					Thread.sleep(randomSleeptime());
					sonar = PacketReceiver.mAmigoInfo.getSonars();
					stall = PacketReceiver.mAmigoInfo.getstall();
					forward = true;
					
					chectStall();
				}
				
				while( sonar[4]<550 && stall==0 && active ){
					Comm.setTransVelocity(0);
					Comm.setRotVelocity(rotV);
					Thread.sleep(randomSleeptime());
					sonar = PacketReceiver.mAmigoInfo.getSonars();
					stall = PacketReceiver.mAmigoInfo.getstall();
					forward = true;
					
					chectStall();
				}
				
				while( sonar[0]<300 && stall==0 && active ){
					Comm.setTransVelocity(0);
					Comm.setRotVelocity((-1)*rotV);
					Thread.sleep(300);
					sonar = PacketReceiver.mAmigoInfo.getSonars();
					stall = PacketReceiver.mAmigoInfo.getstall();
					forward = true;
					
					chectStall();
				}
				
				while( sonar[5]<300 && stall==0 && active ){
					Comm.setTransVelocity(0);
					Comm.setRotVelocity(rotV);
					Thread.sleep(300);
					sonar = PacketReceiver.mAmigoInfo.getSonars();
					stall = PacketReceiver.mAmigoInfo.getstall();
					forward = true;
					
					chectStall();
				}
				
				if( forward && stall==0 ){
					Comm.setTransVelocity(transV);
					Comm.setRotVelocity(0);
					Thread.sleep(100);
					sonar = PacketReceiver.mAmigoInfo.getSonars();
					stall = PacketReceiver.mAmigoInfo.getstall();
					forward = false;
				}
				
				chectStall();
				
				sonar = PacketReceiver.mAmigoInfo.getSonars();
				stall = PacketReceiver.mAmigoInfo.getstall();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void chectStall(){
		if( stall!=0 ){
			try{
				int randrotv = randomRotate();
				for( int i=0; i<20; ++i){
					Comm.setTransVelocity((-1)*transV);
					Comm.setRotVelocity(randrotv);
					Thread.sleep(100);
					
					sonar = PacketReceiver.mAmigoInfo.getSonars();
					
					if( sonar[6]<400 ){
						Comm.setTransVelocity(transV);
						Comm.setRotVelocity(rotV);
						Thread.sleep(500);
						break;
					}
					if( sonar[7]<400 ){
						Comm.setTransVelocity(transV);
						Comm.setRotVelocity((-1)*rotV);
						Thread.sleep(500);
						break;
					}
					
					stall = PacketReceiver.mAmigoInfo.getstall();
					
					if( stall!=0 ){
						Comm.setTransVelocity(transV);
						Comm.setRotVelocity(0);
						Thread.sleep(300);
						break;
					}
				}
				
				forward = true;
			}catch( Exception e ){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public int randomRotate(){
		int rand = (int)(Math.random()*2+1);
		if( rand==1 ){
			return rotV;
		}
		else{
			return (-1)*rotV;
		}
	}
	
	public int randomSleeptime(){
		int rand = (int)(Math.random()*3+1);
		int sleep = 0;
		switch( rand+2 ){
			case 3: sleep = 300;
				break;
			case 4: sleep = 400;
				break;
			case 5: sleep = 500;
				break;
			default: 
				break;
		}
		return sleep;
	}
	
	public void endWanderMode() throws Exception{
		active = false;
		Comm.setTransVelocity(0);
		Comm.setRotVelocity(0);
		Comm.setMaxTransVelocity(100);
		Comm.setMaxRotVelocity(10);
	}
	
	public void startWanderMode(){
		active = true;
		new Thread(this).start();
	}

}
