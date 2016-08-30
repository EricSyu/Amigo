package com.control.amigo.drive;

interface AmigoProtocol {
	
	public static final int HEADER1 = 0xFA;
	public static final int HEADER2 = 0xFB;
	
	public static final byte SYNC0 = 0;
	public static final byte SYNC1 = 1;
	public static final byte SYNC2 = 2;
	
	public static final byte PULSE = 0;
	public static final byte OPEN = 1;
	public static final byte CLOSE = 2;
	public static final byte POLLING = 3;
	public static final byte ENABLE = 4;
	public static final byte SETA = 5;
	public static final byte SETV = 6;
	public static final byte SETO = 7;
	public static final byte SETRV = 10;
	public static final byte VEL = 11;
	public static final byte HEAD = 12;
	public static final byte DHEAD = 13;
	public static final byte SAY = 15;
	public static final byte CONFIG = 18;
	public static final byte ENCODER = 19;
	public static final byte RVEL = 21;
	public static final byte DCHEAD = 22;
	public static final byte SETRA = 23;
	public static final byte SONAR = 28;
	public static final byte STOP = 29;
	public static final byte DIGOUT = 30;
	public static final byte VEL2 = 32;
	public static final byte GRIPPER = 33;
	public static final byte ADSEL = 35;
	public static final byte GRIPPERVAL = 36;
	public static final byte IOREQUEST = 40;
	public static final byte PTUPOS = 41;
	public static final byte TTY2 = 42;
	public static final byte GETAUX = 43;
	public static final byte BUMPSTALL = 44;
	public static final byte TCM2 = 45;
	public static final byte SONARCYCLE = 48;
	public static final byte E_STOP = 55;
	public static final byte STEP = 64;
	public static final byte TTY3 = 66;
	public static final byte GETAUX2 = 67;
	public static final byte ROTKP = 82;
	public static final byte ROTKV = 83;
	public static final byte ROTKI = 84;
	public static final byte TRANSKP = 85;
	public static final byte TRANSKV = 87;
	public static final byte TRANSKI = 87;
	public static final byte REVCOUNT = 88;
	public static final byte SOUND = 90;
	public static final byte PLAYLIST = 91;
	public static final byte SOUNDTOG = 92;

    /**
     * Positive int
     */
    public static final byte ARGINT = 0x3B;
    /**
     * Negative int
     */
    public static final byte ARGNINT = 0x1B;
    /**
     * String
     */
    public static final byte ARGSTR = 0x2B;
}
