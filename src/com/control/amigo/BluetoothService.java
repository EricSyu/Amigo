package com.control.amigo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.control.amigo.drive.AmigoCommunication;

public class BluetoothService extends Service {
	public static final String ACTION_CONNECT = "com.control.amigo.action.CONNECT";
	public static final String ACTION_STOP = "com.control.amigo.action.STOP";
	public static final String ACTION_WORK = "com.control.amigo.action.WORK";
	public static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	public static final UUID uuid = UUID.fromString(SPP_UUID);
	public static final String tag = "Bluetooth";
	public static final String Tag_Position = "ConnectAddressPosition";
	
	public static BTState mBTState = BTState.stopped;
	public static AmigoState mAmigoState = AmigoState.stopped;
	public static BTdevicestate mBTdevicestate = BTdevicestate.closed;
	
	private static ArrayList<String> devices = new ArrayList<String>();
	private static BluetoothAdapter btAdapt = null;
	private static BluetoothSocket btSocket;
	private static InputStream btIn = null;
	private static OutputStream btOut = null;
	private String devAddr = null;
	private int position;
	public static String Teleop_setv = "0";
	
	enum BTdevicestate{ closed, opened, connected };
	enum BTState{ stopped, connecting, running };
	enum AmigoState{ stopped, running };
	
	private static ServiceThread serviceThread;
	
	public static AmigoCommunication Amigo;
	public static boolean AmigoRun = false;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

		if( btAdapt==null ) btAdapt = BluetoothAdapter.getDefaultAdapter();
		IntentFilter intent = new IntentFilter();
		intent.addAction(BluetoothDevice.ACTION_FOUND);
		intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(searchDevices, intent);
		
		if( BTisEnabled() ){
			mBTdevicestate = BTdevicestate.opened;
		}
		else if( !BluetoothService.BTisEnabled() ){
			mBTdevicestate = BTdevicestate.closed;
		}
		
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		try{
			String action = intent.getAction();
			if( action.equals(ACTION_CONNECT) ){
				position = intent.getIntExtra(Tag_Position, 0);
				startConnect();	
			}
			else if( action.equals(ACTION_STOP) ){
				stopConnect();
			}
			else if( action.equals(ACTION_WORK) ){
				setBluetoothRearch();
			}
		}catch( Exception e ){
			e.printStackTrace();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void startConnect(){
		serviceThread = new ServiceThread();
		new Thread(serviceThread).start();
		
		mBTState = BTState.connecting;
	}
	
	private void stopConnect(){
		mBTState = BTState.stopped;
		mBTdevicestate = BTdevicestate.opened;
		if( btIn != null ){
			try{
				btSocket.close();
				btSocket = null;
			} catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(searchDevices);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static ArrayList<String> getdevice(){
		return devices;
	}
	
	public static void resetdeveces(){
		devices = new ArrayList<String>();
	}
	
	public static void setBluetoothOpen(){
		btAdapt.enable();
	}
	
	public static void setBluetoothClose(){
		btAdapt.disable();
	}
	
	public static boolean BTisEnabled(){
		return btAdapt.isEnabled();
	}
	
	public static void setBluetoothRearch(){
		btAdapt.cancelDiscovery();
		btAdapt.startDiscovery();
	}
	
	public class ServiceThread implements Runnable {
		
		public ServiceThread( ) {
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				devAddr = devices.get(position).split("\\|")[1];
				btAdapt.cancelDiscovery();
				
				btSocket = btAdapt.getRemoteDevice(devAddr).createRfcommSocketToServiceRecord(uuid);
				btSocket.connect();
				
				synchronized (this) {
					btIn = btSocket.getInputStream();
					btOut = btSocket.getOutputStream();
					Log.e(tag, "connected");
				}
				
				Thread.sleep(1500);
				mBTState = BTState.running;
				mBTdevicestate = BTdevicestate.connected;
			} catch( IOException | InterruptedException e ){
				e.printStackTrace();
				try{
					btSocket.close();
					Thread.sleep(1500);
				} catch(IOException | InterruptedException e1){
					e1.printStackTrace();
				}
				btSocket = null;
				mBTState = BTState.stopped;
				mBTdevicestate = BTdevicestate.opened;
			}
		}
	}
	
	public static void AmigoSwitch( ){
		if( mAmigoState.equals(AmigoState.stopped) ){
			Amigo = new AmigoCommunication(btOut, btIn);
			try {
				Amigo.AmigoStart();
				mAmigoState = AmigoState.running;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("AmigoConn", "Eerror!!!!!!!!");
			}
		}
		else if( mAmigoState.equals(AmigoState.running) ){
			try {
				Amigo.AmigoStop();
				Amigo = null;
				mAmigoState = AmigoState.stopped;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void Teleop_run(String mode, int Teleop_vel){
		if( mode.equals("Trans") ){
			try {
				Amigo.setTransVelocity(Teleop_vel);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if( mode.equals("Rotate") ){
			try {
				Amigo.setRotVelocity(Teleop_vel);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if( mode.equals("setMaxVel") ){
			try {
				Amigo.setMaxTransVelocity(Teleop_vel);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if( mode.equals("setMaxRotVel") ){
			try {
				Amigo.setMaxRotVelocity(Teleop_vel);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private BroadcastReceiver searchDevices = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			Bundle b = intent.getExtras();
			Object[] lstName = b.keySet().toArray();
			
			for( int i=0; i<lstName.length; i++ ){
				String keyName = lstName[i].toString();
				Log.e(keyName, String.valueOf(b.get(keyName)));
			}
			BluetoothDevice device = null;
			
			if( BluetoothDevice.ACTION_FOUND.equals(action) ){
				device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String str = device.getName() + "|" + device.getAddress();
				if( devices.indexOf(str) == -1 )
					devices.add(str);
				if( mBTdevicestate.equals(BTdevicestate.opened) ) 
					BluetoothConnect.adapterdevices.notifyDataSetChanged();
			}
			else if( BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action) ){
				device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				switch( device.getBondState() ){
					case BluetoothDevice.BOND_BONDING:
						Log.d(tag, "正在配對");
						break;
					case BluetoothDevice.BOND_BONDED:
						Log.d(tag, "完成配對");
						break;
					case BluetoothDevice.BOND_NONE:
						Log.d(tag, "取消配對");
					default:
						break;
				}
			}
		}
	};
	
	
}
