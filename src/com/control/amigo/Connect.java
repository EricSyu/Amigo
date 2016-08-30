package com.control.amigo;


import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.control.amigo.BluetoothService.AmigoState;
import com.control.amigo.BluetoothService.BTState;
import com.control.amigo.MonitorService.MobileCamStatus;
import com.control.amigo.MonitorService.PreviewStatus;
import com.example.amigo.R;

public class Connect extends Fragment {
	private ListView ConnectList1,ConnectList2;
	private MyAdapter adapter1,adapter2;
	private Timer timer;
	private Handler handler = new Handler();
	
	private String[] arr1 = new String[]{"RS232藍牙","AmigoBot"};
	private String[] arr2 = new String[]{"Wifi定位","MobileCam"};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View InputFragmentView = inflater.inflate(R.layout.connect_view, container, false);
		ConnectList1 = (ListView) InputFragmentView.findViewById(R.id.connectList1);
		adapter1 = new MyAdapter(getActivity(), arr1);
		ConnectList1.setAdapter(adapter1);
		ConnectList1.setOnItemClickListener(connectlistlst1);
		
		adapter2 = new MyAdapter(getActivity(), arr2);
		ConnectList2 = (ListView) InputFragmentView.findViewById(R.id.connectList2);
		ConnectList2.setAdapter(adapter2);
		ConnectList2.setOnItemClickListener(connectlistlst2);
		
		return InputFragmentView;
	}
	
	private OnItemClickListener connectlistlst1 = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			switch(position){
			case 0:
				if( BluetoothService.mBTState.equals(BluetoothService.BTState.running) ){
					Toast.makeText(getActivity(), "藍牙已連線", Toast.LENGTH_SHORT).show();
				}
				else if( BluetoothService.mBTState.equals(BluetoothService.BTState.stopped) ){
					Intent btserviceIntent = new Intent(BluetoothService.ACTION_WORK);
					getActivity().startService(btserviceIntent);
					
					Intent intent = new Intent();
					intent.setClass(getActivity(), BluetoothConnect.class);
					startActivity(intent);
					getActivity().overridePendingTransition(R.animator.in_from_right, R.animator.out_to_left);
				}
	            break;
			case 1:
				if( BluetoothService.mBTState.equals(BluetoothService.BTState.running) ){
					
					if( BluetoothService.mAmigoState.equals(AmigoState.stopped) ){
						BluetoothService.AmigoSwitch();
					}
					else if( BluetoothService.mAmigoState.equals(AmigoState.running) ){
						BluetoothService.AmigoSwitch();
					}
				}
				else Toast.makeText(getActivity(), "RS232藍牙尚未連線", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
		}
		}
	}; 
	
	private OnItemClickListener connectlistlst2 = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			switch(position){
			case 0:
				if( MonitorService.mWifiPosStatus.equals(MonitorService.WifiPosStatus.stopped) ){
					Intent monitorIntent = new Intent(MonitorService.ACTION_MONITOR_WIFIPOS);
					getActivity().startService(monitorIntent);
				}
				else if( MonitorService.mWifiPosStatus.equals(MonitorService.WifiPosStatus.wificonnected) ){
					Intent monitorIntent = new Intent(MonitorService.ACTION_MONITOR_WIFIPOS_STOP);
					getActivity().startService(monitorIntent);
				}
	            break;
			case 1: 
				if( MonitorService.mPreviewStatus.equals(MonitorService.PreviewStatus.closed) ){
					Intent monitorIntent = new Intent(MonitorService.ACTION_MONITOR_MOBILECAM);
					getActivity().startService(monitorIntent);
				}
				else if( MonitorService.mPreviewStatus.equals(PreviewStatus.opened) ){
					Intent monitorIntent = new Intent(MonitorService.ACTION_MONITOR_MOBILECAM_STOP);
					getActivity().startService(monitorIntent);
				}
				break;
			default:
				break;
		}
		}
	};
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		if( timer==null ){
			timer = new Timer();
			timer.scheduleAtFixedRate(new updataListCheck(), 0, 500);
		}
		super.onResume();
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		timer.cancel();
		timer = null;
		super.onStop();
	}
	
	class updataListCheck extends TimerTask {

		@Override
		public void run() {
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					adapter1.notifyDataSetInvalidated();
					adapter2.notifyDataSetInvalidated();
				}
			});
		}
	}
	
	class MyAdapter extends BaseAdapter {
		private LayoutInflater myInflater;
		private String[] arr;
		
		public MyAdapter( Context c, String[] arr ) {
			// TODO Auto-generated constructor stub
			myInflater = LayoutInflater.from(c);
			this.arr = arr;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return arr.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return arr[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			convertView = myInflater.inflate(R.layout.bluebooth_connect_list, null);
			ImageView imglogo = (ImageView) convertView.findViewById(R.id.imagelogo);
			TextView msg1 = (TextView) convertView.findViewById(R.id.msg1);
			TextView msg2 = (TextView) convertView.findViewById(R.id.msg2);
			ImageView imgcheck = (ImageView) convertView.findViewById(R.id.imagecheck);
			
			if( arr[0].equals("RS232藍牙") ){
				switch( position ){
				case 0: imglogo.setImageResource(R.drawable.bluetooth);
						msg1.setText(arr[0]);
						
						if( BluetoothService.mBTState.equals(BTState.stopped) ){
							msg2.setText("未連線");
							imgcheck.setImageResource(R.drawable.error);
						}
						else if( BluetoothService.mBTState.equals(BTState.connecting) ){
							msg2.setText("連線中...");
							imgcheck.setImageResource(R.drawable.error);
						}
						else if( BluetoothService.mBTState.equals(BTState.running) ){
							msg2.setText("已連線");
							imgcheck.setImageResource(R.drawable.correct);
						}
					break;
				case 1: imglogo.setImageResource(R.drawable.amigobot);
						msg1.setText(arr[1]);
						
						if( BluetoothService.mAmigoState.equals(AmigoState.stopped) ){
							msg2.setText("未連線");
							imgcheck.setImageResource(R.drawable.error);
						}
						else if( BluetoothService.mAmigoState.equals(AmigoState.running) ){
							msg2.setText("已連線");
							imgcheck.setImageResource(R.drawable.correct);
						}
					break;	
				}
			}
			else if( arr[0].equals("Wifi定位") ){
				switch( position ){
				case 0: imglogo.setImageResource(R.drawable.wifiposition);
						msg1.setText(arr[0]);
						
						if( MonitorService.mWifiPosStatus.equals(MonitorService.WifiPosStatus.stopped) ){
							msg2.setText("未連線");
							imgcheck.setImageResource(R.drawable.error);
						}
						else if( MonitorService.mWifiPosStatus.equals(MonitorService.WifiPosStatus.wificonnected) ){
							msg2.setText("已連線");
							imgcheck.setImageResource(R.drawable.correct);
						}
					break;
				case 1: imglogo.setImageResource(R.drawable.mobilecam);
						msg1.setText(arr[1]);
						
						if( MonitorService.mMobileCamStatus.equals(MobileCamStatus.stopped) && MonitorService.mPreviewStatus.equals(PreviewStatus.closed) ){
							msg2.setText("未連線");
							imgcheck.setImageResource(R.drawable.error);
						}
						else if( MonitorService.mMobileCamStatus.equals(MobileCamStatus.connecting) && MonitorService.mPreviewStatus.equals(PreviewStatus.opened) ){
							msg2.setText("與server連線中...");
							imgcheck.setImageResource(R.drawable.error);
						}
						else if( MonitorService.mMobileCamStatus.equals(MobileCamStatus.stopped) && MonitorService.mPreviewStatus.equals(PreviewStatus.opened) ){
							msg2.setText("與server連線失敗");
							imgcheck.setImageResource(R.drawable.error);
						}
						else if( MonitorService.mMobileCamStatus.equals(MobileCamStatus.Camconnected) && MonitorService.mPreviewStatus.equals(PreviewStatus.opened) ){
							msg2.setText("已連線");
							imgcheck.setImageResource(R.drawable.correct);
						}
					break;	
				}
			}
			
			return convertView;
		}
	}
	
}