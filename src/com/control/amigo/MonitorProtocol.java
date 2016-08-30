package com.control.amigo;

interface MonitorProtocol {
	public static final int BluetoothSwitch = 1;
	public static final int AmigoConnSwitch = 2;
	public static final int WifiposSwitch = 3;
	public static final int MobilecamSwitch = 4;
	
	public static final int Trans = 5;
	public static final int Rotate = 6;
	public static final int AbsoluteHeading = 7;
	public static final int MaxTransV = 8;
	public static final int MaxRotV = 9;
	public static final int ResetPosition = 10;
	public static final int WanderMode = 11;
	
	public static final int PlayMusic1 = 101;
	public static final int PlayMusic2 =102; 
	
	public static final int Close = 1;
	public static final int Open = 2;
	public static final int Stoped = 3;
	public static final int Connected = 4;
	public static final int Search = 5;
	
}
