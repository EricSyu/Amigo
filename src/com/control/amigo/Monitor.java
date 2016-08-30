package com.control.amigo;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.control.amigo.drive.AmigoCommunication;
import com.example.amigo.R;

public class Monitor extends Fragment implements OnTouchListener {
	private ImageButton startMonitor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View InputFragmentView;
		String warning = "";
		
		if( AmigoCommunication.WanderModeStatus ){
			InputFragmentView = inflater.inflate(R.layout.unconnect_view, container, false);
			warning = "WanderMode is running!!!";
			
			TextView warningtext = (TextView) InputFragmentView.findViewById(R.id.warningtext);
			warningtext.setText(warning);
		}
		else {
			InputFragmentView = inflater.inflate(R.layout.monitor_view, container, false);
			startMonitor = (ImageButton) InputFragmentView.findViewById(R.id.startmonitor);
			
			startMonitor.setOnTouchListener(this);
			
			if( MonitorService.MonitorMode ){
				startMonitor.setBackgroundResource(R.drawable.monitor_stop);
			}
			else if( !MonitorService.MonitorMode ){
				startMonitor.setBackgroundResource(R.drawable.monitor_start);
			}
		}
		
		return InputFragmentView;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if( v.getId()==R.id.startmonitor ){
			if( !MonitorService.MonitorMode ){
				if( event.getAction()==MotionEvent.ACTION_DOWN ){
					startMonitor.setBackgroundResource(R.drawable.monitor_start_press);
				}
				else if( event.getAction()==MotionEvent.ACTION_UP ){
					Intent intent = new Intent(MonitorService.ACTION_MONITOR_START);
					getActivity().startService(intent);
					
					MonitorService.MonitorMode = true;
					startMonitor.setBackgroundResource(R.drawable.monitor_stop);
				}
			}
			else if( MonitorService.MonitorMode ){
				if( event.getAction()==MotionEvent.ACTION_DOWN ){
					startMonitor.setBackgroundResource(R.drawable.monitor_stop_press);
				}
				else if( event.getAction()==MotionEvent.ACTION_UP ){
					Intent intent = new Intent(MonitorService.ACTION_MONITOR_STOP);
					getActivity().startService(intent);
					
					MonitorService.MonitorMode = false;
					startMonitor.setBackgroundResource(R.drawable.monitor_start);
				}
			}
		}
		return false;
	}
	
}
