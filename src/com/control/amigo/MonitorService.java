package com.control.amigo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.control.amigo.BluetoothService.AmigoState;
import com.control.amigo.BluetoothService.BTState;
import com.control.amigo.BluetoothService.BTdevicestate;
import com.control.amigo.drive.AmigoCommunication;
import com.example.amigo.R;

public class MonitorService extends Service implements MonitorProtocol, Runnable {
	public static final String ACTION_MONITOR_WORK = "com.control.amigo.action.MONITOR_WORK";
	public static final String ACTION_MONITOR_WIFIPOS = "com.control.amigo.action.MONITOR_WIFIPOS";
	public static final String ACTION_MONITOR_WIFIPOS_STOP = "com.control.amigo.action.MONITOR_WIFIPOS_STOP";
	public static final String ACTION_MONITOR_MOBILECAM = "com.control.amigo.action.MONITOR_MOBILECAM";
	public static final String ACTION_MONITOR_MOBILECAM_STOP = "com.control.amigo.action.MONITOR_MOBILECAM_STOP";
	public static final String ACTION_MONITOR_START = "com.control.amigo.action.MONITOR_START";
	public static final String ACTION_MONITOR_STOP = "com.control.amigo.action.MONITOR_STOP";
	
	private Socket wifiSocket;
	private PrintWriter wifiOut = null;
	private WifiManager wifi;
	private List<ScanResult> list;
	private StringBuilder stringBuilder;
	private String wifiPort = "861";
	private static String camPort = "168";
	private String infoPort = "200";
	private String monitorPort = "100";
	private Handler handler, FWviewhandler;
	private MediaPlayer mMediaPlayer;
	public static String serverAddr = "120.105.129.101";
	public static int size, wififlag = 0, camflag = 0, sendfirstRun = 0;
	public static boolean wifiRun = false, camRun = false, MonitorRun = false, PreViewSize = false;
	public static boolean MonitorMode = false;
	
	public static WifiPosStatus mWifiPosStatus = WifiPosStatus.stopped;
	public static MobileCamStatus mMobileCamStatus = MobileCamStatus.stopped;
	public static PreviewStatus mPreviewStatus = PreviewStatus.closed;
	
	enum WifiPosStatus{ stopped, wificonnected }
	enum MobileCamStatus{ stopped, connecting, Camconnected }
	enum PreviewStatus{ closed, opened }
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		serverAddr = FragtabsActivity.ServerIP;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		try{
			String action = intent.getAction();
			if( action.equals(ACTION_MONITOR_WORK) ){
				
			}
			else if( action.equals(ACTION_MONITOR_WIFIPOS) ){
				startWifiPosition();
			}
			else if( action.equals(ACTION_MONITOR_MOBILECAM) ){
				startCam();
			}
			else if( action.equals(ACTION_MONITOR_WIFIPOS_STOP) ){
				stopWifiPosition();
			}
			else if( action.equals(ACTION_MONITOR_MOBILECAM_STOP) ){
				stopCam();
			}
			else if( action.equals(ACTION_MONITOR_START) ){
				startMonitor();
			}
			else if( action.equals(ACTION_MONITOR_STOP) ){
				stopMonitor();
			}
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void startWifiPosition(){
		wifiRun = true;
		wififlag = 0;
		new Wifilevel().start();
	}
	
	public void stopWifiPosition(){
		wififlag = 1;
		unregisterReceiver(wifi_receiver);
	}
	
	private void startCam(){
		camRun = true;
		mMobileCamStatus = MobileCamStatus.connecting;
		sendfirstRun = 0;
		
		FWviewhandler = new Handler();
		if( !MonitorFWManager.isWindowShowing() && mPreviewStatus.equals(PreviewStatus.closed) ){
			FWviewhandler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					MonitorFWManager.createPreviewWindow(getApplicationContext());
					mPreviewStatus = PreviewStatus.opened;
					PreViewSize = false;
				}
			});
		}
	}
	
	private void stopCam(){
		camRun = false;
		mMobileCamStatus = MobileCamStatus.stopped;
		
		FWviewhandler = new Handler();
		if( MonitorFWManager.isWindowShowing() && mPreviewStatus.equals(PreviewStatus.opened) ){
			FWviewhandler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					MonitorFWManager.removePreviewWindow(getApplicationContext());
					mPreviewStatus = PreviewStatus.closed;
					PreViewSize = false;
				}
			});
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if( mWifiPosStatus.equals(WifiPosStatus.wificonnected) ){
			stopWifiPosition();
		}
		if( mMobileCamStatus.equals(MobileCamStatus.Camconnected) || mPreviewStatus.equals(PreviewStatus.opened) ){
			stopCam();
		}
		if( MonitorRun ){
			stopMonitor();
		}
	}
	
	public static String getserverIP(){
		return serverAddr;
	}
	
	public static void setserverIP( String addr ){
		serverAddr = addr;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	class Wifilevel extends Thread{
		@Override
		public void run(){
			while( wifiRun ){
				if( wififlag == 0 ){
					try{
	    				InetAddress severInetAddr=InetAddress.getByName(serverAddr);
	    				wifiSocket=new Socket(severInetAddr, Integer.parseInt(wifiPort));
	        			wifiOut = new PrintWriter(wifiSocket.getOutputStream());
	        			Log.i("wifiPosition", "Connect!!");
	        			
	        			mWifiPosStatus = WifiPosStatus.wificonnected;
	        			
	        			wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
						
						IntentFilter intent = new IntentFilter();
						intent.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
						registerReceiver(wifi_receiver, intent);
						
						wifi.startScan();
	        			
	        			wififlag = 2;
	        		}catch(Exception e){
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        			mWifiPosStatus = WifiPosStatus.stopped;
	        			wifiRun = false;
	        			
	        			handler = new Handler(Looper.getMainLooper());
	        			handler.post(new Runnable() {
	        				
	        				@Override
	        				public void run() {
	        					// TODO Auto-generated method stub
	        					Toast.makeText(getApplicationContext(), "Wifi定位連線失敗",Toast.LENGTH_SHORT).show();
	        				}
	        			});;
	        			
	        		}
				}
				else if( wififlag == 1 ){
					
					try {
						wifiRun = false;
						if( wifiOut != null ){
							wifiOut.close();
							wifiOut = null;
						}
						wifiSocket.close();
						
						mWifiPosStatus = WifiPosStatus.stopped;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.e("WifiPosition", "Socket Close Error!!");
					}
				}
				else if( wififlag == 2 ){
					
				}
			}	
		}
	}
	
	private BroadcastReceiver wifi_receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if( mWifiPosStatus.equals(WifiPosStatus.wificonnected) ){
				list = wifi.getScanResults();
				size = list.size();
				stringBuilder = new StringBuilder();
				for (int i = 0; i < list.size(); i++){
					stringBuilder.append("N"+(list.get(i)).toString()); 
				}
				stringBuilder.append("size:"+size);
				
				wifiOut.println(stringBuilder);
    			wifiOut.flush();
    			
    			wififlag = 0;
    			Log.e("WifiPosition", "sendWifiInfo");
			}
			
		}
	};
	
	public static class CamSendThread extends Thread{        
        private byte byteBuffer[] = new byte[1024];
        private OutputStream Out;        
        private ByteArrayOutputStream byteOut;
        
        public CamSendThread(ByteArrayOutputStream myoutputstream){
                this.byteOut = myoutputstream;
        }
        
	    @Override
		public void run() {
	    	if( camRun ){
	    		try{
		            Socket socket = new Socket(serverAddr, Integer.parseInt(camPort));
		            Out = socket.getOutputStream();
		            mMobileCamStatus = MobileCamStatus.Camconnected;
		            
		            ByteArrayInputStream inputstream = new ByteArrayInputStream(byteOut.toByteArray());
		            int amount;
		            while ((amount = inputstream.read(byteBuffer)) != -1) {
		            	Out.write(byteBuffer, 0, amount);
		            }
		            byteOut.flush();
		            byteOut.close();
		            Out.close();
		            socket.close();
		        } catch (IOException e) {
		            mMobileCamStatus = MobileCamStatus.stopped;
		            camRun = false;
		        }
	    	}
	    }
	}
	
	public class InformationSend extends Thread {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while( MonitorRun ){
				try {
					Socket socket = new Socket(serverAddr, Integer.parseInt(infoPort));
					DataOutputStream out = new DataOutputStream(socket.getOutputStream());
					PrintWriter outString = new PrintWriter(socket.getOutputStream());
					
					String BluetoothStatus = null, AmigoConnStatus = null,
							WifiposStatus = null, MobilecamStatus = null;
					if( BluetoothService.mBTState.equals(BTState.running) ){
						BluetoothStatus = "Connected";
					}
					else if( BluetoothService.mBTdevicestate.equals(BTdevicestate.closed) ){
						BluetoothStatus = "Close";
					}
					else if( BluetoothService.mBTdevicestate.equals(BTdevicestate.opened) ){
						BluetoothStatus = "Open";
					}
					
					if( BluetoothService.mAmigoState.equals(AmigoState.stopped) ){
						AmigoConnStatus = "Stopped";
					}
					else if( BluetoothService.mAmigoState.equals(AmigoState.running) ){
						AmigoConnStatus = "Connected";
					}
					
					if( MonitorService.mWifiPosStatus.equals(WifiPosStatus.stopped) ){
						WifiposStatus = "Stopped";
					}
					else if( MonitorService.mWifiPosStatus.equals(WifiPosStatus.wificonnected) ){
						WifiposStatus = "Connected";
					}
					
					if( MonitorService.mMobileCamStatus.equals(MobileCamStatus.stopped) ){
						MobilecamStatus = "Stopped";
					}
					else if( MonitorService.mMobileCamStatus.equals(MobileCamStatus.Camconnected) ){
						MobilecamStatus = "Connected";
					}
					
					int XPos = 0, YPos = 0, thetaPos = 0, Motor = 0, Stall = 0;
					int[] sonar = new int[]{0,0,0,0,0,0,0,0};
					
					if( BluetoothService.mAmigoState.equals(AmigoState.running) ){
						Motor = (BluetoothService.Amigo.Monitor_getMotor()?1:0);
						Stall = (BluetoothService.Amigo.Monitor_Stall()?1:0);
						XPos = BluetoothService.Amigo.Monitor_XPos();
						YPos = BluetoothService.Amigo.Monitor_YPos();
						thetaPos = BluetoothService.Amigo.Monitor_thetaPos();
						
						sonar = BluetoothService.Amigo.Monitor_getSonar();
						
					}
					
					outString.println(BluetoothStatus);
					outString.flush();
					outString.println(AmigoConnStatus);
					outString.flush();
					outString.println(WifiposStatus);
					outString.flush();
					outString.println(MobilecamStatus);
					outString.flush();
					
					outString.println(String.valueOf(Motor));
					outString.flush();
					outString.println(String.valueOf(Stall));
					outString.flush();
					outString.println(String.valueOf(XPos));
					outString.flush();
					outString.println(String.valueOf(YPos));
					outString.flush();
					outString.println(String.valueOf(thetaPos));
					outString.flush();
					for( int i=0; i<8; ++i ){
						outString.println(String.valueOf(sonar[i]));
						outString.flush();
					}
					
					outString.close();
					out.close();
					socket.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					handler = new Handler(Looper.getMainLooper());
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(getApplicationContext(), "Infomation傳送失敗",Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}
	}
	
	public void MonitorCommunication(){
		try {
			Socket socket = new Socket(serverAddr, Integer.parseInt(monitorPort));
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			DataInputStream in = new DataInputStream(socket.getInputStream());
			
			int request = 0;
			request = in.readInt();
			
			switch( request ){
				case BluetoothSwitch:
					int Switch1 = in.readInt();
					if( Switch1 == Close ){
						if( BluetoothService.mBTState.equals(BTState.running) ){
							if( BluetoothService.mAmigoState.equals(AmigoState.running) ){
								BluetoothService.AmigoSwitch();
							}
							
							Intent intentBluetooth = new Intent(BluetoothService.ACTION_STOP);
							startService(intentBluetooth);
							
							Thread.sleep(500);
							
							BluetoothService.resetdeveces();
							BluetoothService.setBluetoothClose();
							Thread.sleep(1000);
							BluetoothService.mBTdevicestate = BTdevicestate.closed;
						}
					}
					else if( Switch1 == Open ){
						BluetoothService.setBluetoothOpen();
						Thread.sleep(3000);
						BluetoothService.mBTdevicestate = BTdevicestate.opened;
					}
					else if( Switch1 == Search ){
						Intent intent = new Intent(getApplicationContext(), BluetoothConnect.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
						
						if( BluetoothService.mBTState.equals(BTState.stopped) ){
							
							while( true ){
								String str = "";
								PrintWriter outString = new PrintWriter(out);
								
								BluetoothService.setBluetoothRearch();
								Thread.sleep(5000);
								
								if( !BluetoothService.getdevice().isEmpty() ){
									for( int i=0; i<BluetoothService.getdevice().size(); ++i ){
										str = str + BluetoothService.getdevice().get(i) + "_";
									}
									outString.println(str);
									outString.flush();
									outString.close();
									break;
								}
							}
						}
					}
					else if( Switch1 == Connected ){
						int position = in.readInt();
						if( BluetoothService.mBTState.equals(BluetoothService.BTState.stopped) ){
							Intent intent2 = new Intent(BluetoothService.ACTION_CONNECT);
							intent2.putExtra(BluetoothService.Tag_Position, position);
							startService(intent2);
						}
						sendBroadcast(new Intent("CloseAction"));
					}
					break;
				case AmigoConnSwitch:
					int Switch2 = in.readInt();
					if( Switch2 == Connected ){
						if( BluetoothService.mBTState.equals(BluetoothService.BTState.running) ){
							
							if( BluetoothService.mAmigoState.equals(AmigoState.stopped) ){
								BluetoothService.AmigoSwitch();
							}
						}
					}
					break;
				case WifiposSwitch:
					int Switch3 = in.readInt();
					if( Switch3 == Stoped ){
						if( MonitorService.mWifiPosStatus.equals(MonitorService.WifiPosStatus.wificonnected) ){
							Intent monitorIntent = new Intent(MonitorService.ACTION_MONITOR_WIFIPOS_STOP);
							startService(monitorIntent);
						}
					}
					else if( Switch3 == Connected ){
						if( MonitorService.mWifiPosStatus.equals(MonitorService.WifiPosStatus.stopped) ){
							Intent monitorIntent = new Intent(MonitorService.ACTION_MONITOR_WIFIPOS);
							startService(monitorIntent);
						}
					}
					break;
				case MobilecamSwitch:
					int Switch4 = in.readInt();
					if( Switch4 == Stoped ){
						if( MonitorService.mPreviewStatus.equals(PreviewStatus.opened) ){
							Intent monitorIntent = new Intent(MonitorService.ACTION_MONITOR_MOBILECAM_STOP);
							startService(monitorIntent);
						}
					}
					else if( Switch4 == Connected ){
						if( MonitorService.mPreviewStatus.equals(MonitorService.PreviewStatus.closed) ){
							Intent monitorIntent = new Intent(MonitorService.ACTION_MONITOR_MOBILECAM);
							startService(monitorIntent);
						}
					}
					break;
				case Trans:
					int vel = in.readInt();
					BluetoothService.Amigo.setTransVelocity(vel);
					break;
				case Rotate:
					int rotvel = in.readInt();
					BluetoothService.Amigo.setRotVelocity(rotvel);
					break;
				case AbsoluteHeading:
					int heading = in.readInt();
					BluetoothService.Amigo.setAbsoluteHeading(heading);
					break;
				case MaxTransV:
					int MaxTransVel = in.readInt();
					if( MaxTransVel<0 ) BluetoothService.Amigo.setMaxTransVelocity((-1)*MaxTransVel);
					else BluetoothService.Amigo.setMaxTransVelocity(MaxTransVel);
					break;
				case MaxRotV:
					int MaxRotVel = in.readInt();
					if( MaxRotVel<0 ) BluetoothService.Amigo.setMaxRotVelocity((-1)*MaxRotVel);
					else BluetoothService.Amigo.setMaxRotVelocity(MaxRotVel);
					break;
				case ResetPosition:
					BluetoothService.Amigo.resetPosition();
					break;
				case WanderMode:
					int Switch5 = in.readInt();
					if( Switch5 == Open ){
						BluetoothService.Amigo.startWanderMode();
						AmigoCommunication.WanderModeStatus = true;
					}
					else if( Switch5 == Close ){
						BluetoothService.Amigo.stopWanderMode();
						AmigoCommunication.WanderModeStatus = false;
					}
					break;
				case PlayMusic1:
					int Switch6 = in.readInt();
					if( Switch6 == Open ){
						mMediaPlayer = MediaPlayer.create(this, R.raw.visual);
						mMediaPlayer.setLooping(true);
						mMediaPlayer.start();
					}
					else if( Switch6 == Close ){
						mMediaPlayer.stop();
						mMediaPlayer.release();
						mMediaPlayer = null;
					}
				case PlayMusic2:
					int Switch7 = in.readInt();
					if( Switch7 == Open ){
						mMediaPlayer = MediaPlayer.create(this, R.raw.obstacle);
						mMediaPlayer.setLooping(true);
						mMediaPlayer.start();
					}
					else if( Switch7 == Close ){
						mMediaPlayer.stop();
						mMediaPlayer.release();
						mMediaPlayer = null;
					}
			}
			
			in.close();
			out.close();
			socket.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(getApplicationContext(), "Monitor連線失敗",Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	public void startMonitor(){
		Intent btserviceIntent = new Intent(BluetoothService.ACTION_WORK);
		startService(btserviceIntent);
		
		MonitorRun = true;
		new Thread(this).start();
		new InformationSend().start();
	}
	
	public void stopMonitor(){
		MonitorRun = false;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while( MonitorRun ){
			MonitorCommunication();
		}
	}
	
}
