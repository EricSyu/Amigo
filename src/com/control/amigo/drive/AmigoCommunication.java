package com.control.amigo.drive;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

public class AmigoCommunication implements AmigoProtocol, Runnable {
	
	private AmigoPulse mPulse = null;
	private PacketReceiver receiver = null;
	private WanderMode wander;
	
	private Vector commandBuffer = new Vector(15,2);
	private DataOutputStream CommOut = null;
	private DataInputStream CommIn = null;
	private Thread thread;
	
	public static AmigoCommState mAmigoCommState = AmigoCommState.STOP;
	public static boolean WanderModeStatus = false;
	
	enum AmigoCommState{ STOP, RUNNING }
	
	private final int VEL2_DIVISOR = 20;
	
	public AmigoCommunication( OutputStream out , InputStream in ) {
		// TODO Auto-generated constructor stub
		CommOut = new DataOutputStream(out);
		CommIn = new DataInputStream(in);
		mPulse = new AmigoPulse(this);
		receiver = new PacketReceiver(CommIn);
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while( mAmigoCommState.equals(AmigoCommState.RUNNING) ){
			try {
				sendIntArray(getCommand());
				pause();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void AmigoStart() throws Exception{
		mAmigoCommState = AmigoCommState.RUNNING;
		sendconnectcode();
		pause();
		sendIntArray( buildPacket(new byte[] { SYNC0 }) );
		pause();
		sendIntArray( buildPacket(new byte[] { SYNC1 }) );
		pause();
		sendIntArray( buildPacket(new byte[] { SYNC2 }) );
		pause();
		sendIntArray( buildPacket(new byte[] { OPEN }) );
		pause();
		sendIntArray( buildPacket(new byte[] { ENABLE, ARGINT, 1, 0 }) );
		pause();
		
		mPulse.startPulse();
		receiver.startReceive();
		thread = new Thread(this);
		thread.start();
	}
	
	private int[] getCommand(){
		int[] toReturn;
		if (commandBuffer.isEmpty()){
			toReturn = null;
			}
		else {
			toReturn = (int[])commandBuffer.remove(0);
		}
		return toReturn;
	} 
	
	private void sendIntArray(int[] command) throws IOException{
		if (command!=null){
			int commandLength = command.length;
			for (int i = 0; i < commandLength; i++){
				CommOut.writeByte(command[i]);
			}
			CommOut.flush();
		}
	}
	
	public synchronized void addCommand(int[] newCommand){
		commandBuffer.add(newCommand);
	}
	
	public void pause(){
		try{
			Thread.sleep(50);
		} catch (Exception e){
			System.out.println("Threading Error: Thread killed.");
		}
	}
	
	public void sendconnectcode() throws Exception {
		int[] connectcode = new int[5];
		connectcode[0] = 'w';
		connectcode[1] = 'M';
		connectcode[2] = 'S';
		connectcode[3] = '2';
		connectcode[4] = '\r';
		sendIntArray(connectcode);
	}
	
    public int[] buildPacket(byte[] packet) throws Exception{
        byte[] fPacket = new byte[packet.length + 5];
        fPacket[0] = (byte)HEADER1;
        fPacket[1] = (byte)HEADER2;
        fPacket[2] = (byte)(packet.length+2);
        for(int i = 0; i < packet.length; i++) {
            fPacket[i+3] = packet[i]; 
        }
        int chksum = calculateCheckSum(fPacket);
		fPacket[fPacket.length-2] = (byte)((chksum >>> 8) & 0xff);
        fPacket[fPacket.length-1] = (byte)(chksum & 0xff);
        
        int[] Intfpacket = new int[fPacket.length];
        for(int i = 0; i < fPacket.length; i++) {
        	Intfpacket[i] = (fPacket[i] & 0xff);
        }
		return Intfpacket;
	}
	
	public static int calculateCheckSum(byte[] packet) {
		int n = packet.length-5;
		int i = 3;
		int c = 0;
		while(n > 1) {
			c += ((packet[i] & 0xff)<<8 | (packet[i+1] & 0xff)); 
			c = (c & 0xffff);
			n -= 2;
			i += 2;
		}
		if(n > 0)
			c = (c ^ (packet[i] & 0xff));
		return c;
	}
	
	public void sendfromDataToCommand() throws IOException{		
		int[] switchcode = new int[4];
		switchcode[0] = '|';
		switchcode[1] = '|';
		switchcode[2] = '|';
		switchcode[3] = '\r';
		sendIntArray(switchcode);
	}
	
	public void senddisconnectCode() throws IOException{
		int[] disconnectcode = new int[4];
		disconnectcode[0] = 'W';
		disconnectcode[1] = 'M';
		disconnectcode[2] = 'D';
		disconnectcode[3] = '\r';
		sendIntArray(disconnectcode);
	}
	
	public void AmigoStop() throws Exception{
		sendIntArray( buildPacket(new byte[]{ CLOSE }) ); 
		pause();
        sendfromDataToCommand();
        pause();
        senddisconnectCode();
        pause();
        
        mPulse.endPulse();
		receiver.endUpdate();
		mAmigoCommState = AmigoCommState.STOP;
		
        try{
        	CommIn = null;
            CommOut = null;
        }catch(Exception e){
        	e.printStackTrace();
        }
        
	}
	
	public void sendPulse() throws Exception{
		addCommand( buildPacket(new byte[]{ PULSE }) );
	}
	
	public void startWanderMode() throws Exception{
		wander = new WanderMode(this);
		wander.startWanderMode();
	}
	
	public void stopWanderMode() throws Exception{
		wander.endWanderMode();
		wander = null;
	}
	
    public void enableMotors(boolean enabled) throws Exception {
    	if (enabled) {
    		byte[] packet = new byte[] { ENABLE, ARGINT, 1, 0 };
    		addCommand( buildPacket(packet) );
    	} else {
    		byte[] packet = new byte[] { ENABLE, ARGINT, 0, 0 };
    		addCommand( buildPacket(packet) );
    	}
    }

    public void setTransAccel(int accel) throws Exception {
	    byte[] packet = null;
	    if (accel > 0) {
	    	packet = new byte[] { SETA, ARGINT, getLSB(accel), getMSB(accel) };
	    } else {
	    	int abs_accel = Math.abs(accel);
	    	packet = new byte[] { SETA, ARGNINT, getLSB(abs_accel),getMSB(abs_accel) };
	    }
	    addCommand( buildPacket(packet) );
    }

    public void setMaxTransVelocity(int maxVel) throws Exception {
    	byte[] packet = new byte[] { SETV, ARGINT, getLSB(maxVel),
    			getMSB(maxVel) };
    	addCommand( buildPacket(packet) );
    }

    public void resetPosition() throws Exception {
    	byte[] packet = new byte[] { SETO };
    	addCommand( buildPacket(packet) );
    }

    public void setMaxRotVelocity(int maxVel) throws Exception {
    	byte[] packet = new byte[] { SETRV, ARGINT, getLSB(maxVel), getMSB(maxVel) };
    	addCommand( buildPacket(packet) );
    }

    public void setTransVelocity(int vel) throws Exception {
    	byte[] packet = null;
    	if (vel > 0) {
    		packet = new byte[] { VEL, ARGINT, getLSB(vel), getMSB(vel) };
    	} else {
    		int abs_vel = Math.abs(vel);
    		packet = new byte[] { VEL, ARGNINT, getLSB(abs_vel), getMSB(abs_vel) };
    	}
    	addCommand( buildPacket(packet) );
    }

    public void setAbsoluteHeading(int heading) throws Exception {
    	byte[] packet = new byte[] { HEAD, ARGINT, getLSB(heading),
    			getMSB(heading) };
    	addCommand( buildPacket(packet) );
    }

    public void setRelativeHeading(int heading) throws Exception {
    	byte[] packet = null;
    	if (heading > 0) {
    		packet = new byte[] { DHEAD, ARGINT, getLSB(heading),getMSB(heading) };
    	} else {
    		int abs_heading = Math.abs(heading);    
    		packet = new byte[] { DHEAD, ARGNINT, getLSB(abs_heading), getMSB(abs_heading) };
    	}
    	addCommand( buildPacket(packet) );
    }

    public void setRotVelocity(int vel) throws Exception {
    	byte[] packet = null;
    	if (vel > 0) {
    		packet = new byte[] { RVEL, ARGINT, getLSB(vel), getMSB(vel) };
    	} else {
    		int abs_vel = Math.abs(vel);
    		packet = new byte[] { RVEL, ARGNINT, getLSB(abs_vel),getMSB(abs_vel) };
    	}
    	addCommand( buildPacket(packet) );
    }

    public void setRotAccel(int accel) throws Exception {
    	byte[] packet = null;
    	if (accel > 0) {
    		packet = new byte[] { SETRA, ARGINT, getLSB(accel), getMSB(accel) };
    	} else {
    		int abs_accel = Math.abs(accel);
    		packet = new byte[] { SETRA, ARGNINT, getLSB(abs_accel), getMSB(abs_accel) };
    	}
    	addCommand( buildPacket(packet) );
    }

    public void enableSonars(boolean enabled) throws Exception {
    	byte[] packet = null;
    	if (enabled) {
    		packet = new byte[] { SONAR, ARGINT, 1, 0 };
    	} else {
    		packet = new byte[] { SONAR, ARGINT, 0, 0 };
    	}
    	addCommand( buildPacket(packet) );
    }

    public void changeSonarCycle(int time) throws Exception {
    	byte[] packet = new byte[] { SONARCYCLE, ARGINT, getLSB(time), getMSB(time) };
    }

    public void stopRobot() throws Exception {
            byte[] packet = new byte[] { STOP, 0 };
            addCommand( buildPacket(packet) );
    }

    public void setWheelVelocity(int leftWheel, int rightWheel) throws Exception {
    	byte leftSpeed, rightSpeed;
    	if (((1.0 * leftWheel) / VEL2_DIVISOR) > 127) {
    		leftSpeed = 127;
    	} else if (((1.0 * leftWheel) / VEL2_DIVISOR) < -128) {
    		leftSpeed = -128;
    	} else {
    		leftSpeed = (byte) Math.round((1.0 * leftWheel) / VEL2_DIVISOR);
    	}
    	if (((1.0 * rightWheel) / VEL2_DIVISOR) > 127) {
    		rightSpeed = 127;
    	} else if (((1.0 * rightWheel) / VEL2_DIVISOR) < -128) {
    		rightSpeed = -128;
    	} else {
    		rightSpeed = (byte) Math.round((1.0*rightWheel) / VEL2_DIVISOR);
    	}
    	byte[] packet = new byte[] { VEL2, ARGINT, rightSpeed, leftSpeed };
    	addCommand( buildPacket(packet) );
    }

    public void emergencyStop() throws Exception {
    	byte[] packet = new byte[] { E_STOP, 0 };
    	addCommand( buildPacket(packet) );
    }

    public void setRotProportional(int kP) throws Exception {
    	byte[] packet = new byte[] { ROTKP, ARGINT, getLSB(kP), getMSB(kP) };
    	addCommand( buildPacket(packet) );
    }

    public void setRotDerivative(int kD) throws Exception {
    	byte[] packet = new byte[] { ROTKV, ARGINT, getLSB(kD), getMSB(kD) };
    	addCommand( buildPacket(packet) );
    }

    public void setRotIntegral(int kI) throws Exception {
    	byte[] packet = new byte[] { ROTKI, ARGINT, getLSB(kI), getMSB(kI) };
    	addCommand( buildPacket(packet) );
    }

    public void setTransProportional(int kP) throws Exception {
    	byte[] packet = new byte[] { TRANSKP, ARGINT, getLSB(kP), getMSB(kP) };
    	addCommand( buildPacket(packet) );
    }

    public void setTransDerivative(int kD) throws Exception {
    	byte[] packet = new byte[] { TRANSKV, ARGINT, getLSB(kD), getMSB(kD) };
    	addCommand( buildPacket(packet) );
    }

    public void setTransIntegral(int kI) throws Exception {
    	byte[] packet = new byte[] { TRANSKI, ARGINT, getLSB(kI), getMSB(kI) };
    	addCommand( buildPacket(packet) );
    }

    public void setPolling() throws Exception {
    	byte[] packet = new byte[]{POLLING, ARGSTR, 0,1,2,3,4,5,6,7};
//    	 addCommand( buildPacket(packet) );
    }

    private final byte getLSB(int integer) {
        return (byte) (integer & 0xff);
    }

    private final byte getMSB(int integer) {
        return (byte) ((integer >>> 8) & 0xff);
    }
    
    public String getMotor(){
    	return "Motor:"+PacketReceiver.mAmigoInfo.isMotor();
    }
    
    public String getBattery(){
    	double bettery = PacketReceiver.mAmigoInfo.getBattery()/10.0;
    	return "Battery:"+ String.valueOf(bettery) + "Volts";
    }
    
    public String getStall(){
    	return "Stall:" + PacketReceiver.mAmigoInfo.getstall();
    }
    
    public String getPosition(){
    	return "(X,Y)=("+String.valueOf(PacketReceiver.mAmigoInfo.getXPos())
    			+","+String.valueOf(PacketReceiver.mAmigoInfo.getYPos())+")"; 
    }
    
    public String getSonar1(){
    	return "Sonar1:"+String.valueOf(PacketReceiver.mAmigoInfo.getSonars()[0]);
    }
    
    public String getSonar2(){
    	return "Sonar2:"+String.valueOf(PacketReceiver.mAmigoInfo.getSonars()[1]);
    }
    
    public String getSonar3(){
    	return "Sonar3:"+String.valueOf(PacketReceiver.mAmigoInfo.getSonars()[2]);
    }
    
    public String getSonar4(){
    	return "Sonar4:"+String.valueOf(PacketReceiver.mAmigoInfo.getSonars()[3]);
    }
    
    public String getSonar5(){
    	return "Sonar5:"+String.valueOf(PacketReceiver.mAmigoInfo.getSonars()[4]);
    }
    
    public String getSonar6(){
    	return "Sonar6:"+String.valueOf(PacketReceiver.mAmigoInfo.getSonars()[5]);
    }
    
    public String getSonar7(){
    	return "Sonar7:"+String.valueOf(PacketReceiver.mAmigoInfo.getSonars()[6]);
    }
    
    public String getSonar8(){
    	return "Sonar8:"+String.valueOf(PacketReceiver.mAmigoInfo.getSonars()[7]);
    }
    
    public double getVel(){
    	return PacketReceiver.mAmigoInfo.getVelocity();
    }
    
    public double getRightVel(){
    	return PacketReceiver.mAmigoInfo.getRightVel();
    }
	
    public boolean Monitor_getMotor(){
    	return PacketReceiver.mAmigoInfo.isMotor();
    }
    
    public boolean Monitor_Stall(){
    	if( PacketReceiver.mAmigoInfo.getstall()==1 ){
    		return true;
    	}
    	else return false;
    }
    
    public int Monitor_XPos(){
    	return (int)PacketReceiver.mAmigoInfo.getXPos();
    }
    
    public int Monitor_YPos(){
    	return (int)PacketReceiver.mAmigoInfo.getYPos();
    }
    
    public int Monitor_thetaPos(){
    	return (int)PacketReceiver.mAmigoInfo.getThetaPos();
    }
    
    public int[] Monitor_getSonar(){
    	return PacketReceiver.mAmigoInfo.getSonars();
    }
    
}
