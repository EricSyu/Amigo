package com.control.amigo.drive;

import java.io.DataInputStream;
import java.io.IOException;

import android.util.Log;

public class PacketReceiver extends Thread {
	
	public static AmigoInfo mAmigoInfo;
	
	private DataInputStream CommIn = null;
	private boolean receiving = false;
	
	private double xPos = 0.0, yPos = 0.0, thetaPos = 0.0;
    private int oldX = 0, oldY = 0;
    private final double WHEEL_COOR_CONV = 1.0;
    private final double WHEEL_ANGLE_CONV = 0.001534;
    private final double SPEED_CONV = 1.0;
    private final double CONTROL_CONV = 0.001534;
	private final int SONAR_RANGE_CONV = 1;
    
	public PacketReceiver ( DataInputStream CommIn ){
		this.CommIn = CommIn;
		mAmigoInfo = new AmigoInfo();
	}
	
	@Override
	public synchronized void run(){
		int newData = 0;
        int counter = 0;
		byte[] dataBuffer = new byte[100];
		while (receiving){
			try {
				newData = CommIn.read();
				
				if( newData==0xFA ){
					dataBuffer[counter] = (byte)(newData);
            		counter++;
            		
            		newData = CommIn.read();
            		if( newData==0xFB ){
            			dataBuffer[counter] = (byte)(newData);
                		counter++;
                		
                		newData = CommIn.read();
                		dataBuffer[counter] = (byte)(newData);
                		counter++;
                		
                		int amount = newData;
                		for( int i=1; i<=amount; ++i ){
                			newData = CommIn.read();
                			dataBuffer[counter] = (byte)(newData);
                    		counter++;
                		}
                		processPacket(dataBuffer);
                		dataBuffer = new byte[100];
                		counter = 0;
            		}
            	}
			} catch (IOException e) {
				e.printStackTrace();
				Log.e("receive", "Data Error:1");
			}

		}
	}
	
	public void startReceive(){
		receiving = true;
		this.start();
	}
	
	public void endUpdate(){
		receiving = false;
		mAmigoInfo = null;
	}
	
	public synchronized void processPacket( byte[] packet ) {
        try {
        if (packet.length > 20) {
                int pointer = 3;
                int status = byteToInt(packet[pointer]);
                if (status == 0x32) {
                    mAmigoInfo.setMotorStatus(false);
                } else if (status == 0x33) {
                    mAmigoInfo.setMotorStatus(true);
                }
                
                pointer++;
                boolean taintedOdometry = false;
                int newX = (byteToInt(packet[pointer++], packet[pointer++]) & 0x7ff);
                int newY = (byteToInt(packet[pointer++], packet[pointer++]) & 0x7ff);
                int theta = byteToInt(packet[pointer++], packet[pointer++]);
                if(xPos != Double.MAX_VALUE) {
                    double change = updatePosition(oldX, newX) * WHEEL_COOR_CONV;
                    oldX = newX;
                    if( change > 10 || change < -10 ) {
                        taintedOdometry = true;
                    }
                    else {
                        xPos += change;
                    }
                } else if(xPos == Double.MAX_VALUE) {
                    xPos = 0;
                    oldX = 0;
                }
                
                if(yPos != Double.MAX_VALUE) {
                    double change = updatePosition(oldY, newY) * WHEEL_COOR_CONV;
                    oldY = newY;
                    if( change > 10 || change < -10 ) {
                        
                        taintedOdometry = true;
                    }
                    else {
                        yPos += change;
                    }
                } else if(yPos == Double.MAX_VALUE) {
                    yPos = 0;
                    oldY = 0;
                } 
                thetaPos = theta *WHEEL_ANGLE_CONV * (180.0/Math.PI);
                mAmigoInfo.setXPos(xPos);
                mAmigoInfo.setYPos(yPos);
                mAmigoInfo.setThetaPos(thetaPos);
                mAmigoInfo.setTaintedOdometryValues(taintedOdometry);
                 
                double leftVel = SPEED_CONV
                        * byteToInt(packet[pointer++], packet[pointer++]);
                double rightVel = SPEED_CONV
                        * byteToInt(packet[pointer++], packet[pointer++]);
                double velocity = (leftVel + rightVel) / 2.0;
                mAmigoInfo.setLeftVel(leftVel);
                mAmigoInfo.setRightVel(rightVel);
                mAmigoInfo.setVelocity(velocity);

                double battery = byteToInt(packet[pointer++]) / 10;
                mAmigoInfo.setBattery(battery);

                int stall = byteToInt(packet[pointer++], packet[pointer++]);
                mAmigoInfo.setstall(stall);
                double control = byteToInt(packet[pointer++], packet[pointer++])
                        * CONTROL_CONV;
                
                byte temp = packet[pointer++];
                byte temp2 = packet[pointer++];
                int PTU = byteToInt(temp, temp2);
                int compass = byteToInt(packet[pointer++]); 
                                                            
                int nrSonars = byteToInt(packet[pointer++]);
                int[] sonars = new int[8];
                for (int i = 0; i < nrSonars; i++) {
                    int sonar = byteToInt(packet[pointer++]);
                    sonars[sonar] = byteToInt(packet[pointer++], packet[pointer++])*SONAR_RANGE_CONV;
                }
                mAmigoInfo.setSonars(sonars);
        }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
	
	private int updatePosition(int from, int to) {
        int movement = to - from;
        return movement;
    }
	
	private int byteToInt(byte lsb, byte msb) {
        return (lsb & 0xff) | ((msb & 0xff) << 8);
    }

    private int byteToInt(byte lsb) {
        return lsb & 0xff;
    }
	
}
