package com.control.amigo.drive;

public class AmigoInfo {

    protected int PTU, stall;

    protected boolean motor, taintedOdometry;

    protected double battery, xPos, yPos, thetaPos, leftVel, rightVel, velocity, control, lastX, lastY, lastTheta;

    protected int[] sonars;
    
    public AmigoInfo() {
        sonars = new int[8];
    }
    
    public boolean isOdomodometryTainted() {
        return taintedOdometry;
    }
    
    public boolean isMotor() {
        return motor;
    }
    
    public void setMotorStatus(boolean motorStopped) {
        this.motor = motorStopped;
    }

    public double getBattery() {
        return battery;
    }
    
    public double getVelocity() {
        return velocity;
    }

    public double getControl() {
        return control;
    }

    public double getLeftVel() {
        return leftVel;
    }

    public int getPTU() {
        return PTU;
    }

    public double getRightVel() {
        return rightVel;
    }

    public double getThetaPos() {
        return thetaPos;
    }

    public double getXPos() {
        return xPos;
    }

    public double getYPos() {
        return yPos;
    }

    public int[] getSonars() {
        return sonars;
    }
    
    public int getstall(){
    	return stall;
    }

    public void setBattery(double battery) {
        this.battery = battery;
    }

    public void setLeftVel(double leftVel) {
        this.leftVel = leftVel;
    }

    public void setRightVel(double rightVel) {
        this.rightVel = rightVel;
    }

    public void setSonars(int[] sonars) {
    	for( int i=0; i<8; ++i ){
    		if( sonars[i]!=0 ){
    			this.sonars[i] = sonars[i];
    		}
    	}
    }

    public void setThetaPos(double thetaPos) {
        this.thetaPos = thetaPos;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public void setXPos(double pos) {
        xPos = pos;
    }

    public void setYPos(double pos) {
        yPos = pos;
    }
    
    public void setTaintedOdometryValues(boolean taintedOdometry) {
        this.taintedOdometry = taintedOdometry;
    }
    
    public void setstall(int x){
    	stall=x;
    }
}
