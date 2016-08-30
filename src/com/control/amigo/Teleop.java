package com.control.amigo;


import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.control.amigo.BluetoothService.AmigoState;
import com.control.amigo.BluetoothService.BTState;
import com.control.amigo.drive.AmigoCommunication;
import com.example.amigo.R;

public class Teleop extends Fragment implements OnTouchListener {
	private TextView veltxt,setvtxt;
	private ImageButton setvadd,setvsub,up,down,left,right;
	private int Teleop_Vel=0, Teleop_RotVel_unit=18;
	private Timer timer = null;
	private Handler handler = new Handler();
	private boolean isNeedAdd = false, isNeedSub = false;
	
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
		if( BluetoothService.mBTState == BTState.stopped || BluetoothService.mBTState == BTState.connecting ){
			warning = "ÂÅ¤ú";
			if( BluetoothService.mAmigoState == AmigoState.stopped ){
				warning = warning + "¡BAmigoBot";
			}
		}
		else {
			if( BluetoothService.mAmigoState == AmigoState.stopped ){
				warning = warning + "AmigoBot";
			}
		}
		
		if( (BluetoothService.mBTState == BTState.stopped || BluetoothService.mBTState == BTState.connecting) 
				|| BluetoothService.mAmigoState == AmigoState.stopped ){
			InputFragmentView = inflater.inflate(R.layout.unconnect_view, container, false);
			warning = warning + "©|¥¼³s½u";
			
			TextView warningtext = (TextView) InputFragmentView.findViewById(R.id.warningtext);
			warningtext.setText(warning);
		}
		else if( AmigoCommunication.WanderModeStatus ){
			InputFragmentView = inflater.inflate(R.layout.unconnect_view, container, false);
			warning = "WanderMode is running!!!";
			
			TextView warningtext = (TextView) InputFragmentView.findViewById(R.id.warningtext);
			warningtext.setText(warning);
		}
		else if( MonitorService.MonitorMode ){
			InputFragmentView = inflater.inflate(R.layout.unconnect_view, container, false);
			warning = "MonitorMode is running!!!";
			
			TextView warningtext = (TextView) InputFragmentView.findViewById(R.id.warningtext);
			warningtext.setText(warning);
		}
		else{
			InputFragmentView = inflater.inflate(R.layout.teleop_view, container, false);
			veltxt = (TextView) InputFragmentView.findViewById(R.id.veltxt);
			setvtxt = (TextView) InputFragmentView.findViewById(R.id.setvtxt);
			setvadd = (ImageButton) InputFragmentView.findViewById(R.id.setvadd);
			setvsub = (ImageButton) InputFragmentView.findViewById(R.id.setvsub);
			up = (ImageButton) InputFragmentView.findViewById(R.id.teleop_up);
			down = (ImageButton) InputFragmentView.findViewById(R.id.teleop_down);
			left = (ImageButton) InputFragmentView.findViewById(R.id.teleop_left);
			right = (ImageButton) InputFragmentView.findViewById(R.id.teleop_right);
			
			veltxt.setText("0");
			Teleop_Vel = Integer.valueOf(BluetoothService.Teleop_setv);
			if( Teleop_Vel ==1000 ) setvtxt.setTextSize(13);
			else if( Teleop_Vel>=100 ) setvtxt.setTextSize(16);
			else setvtxt.setTextSize(25);
			setvtxt.setText(BluetoothService.Teleop_setv);
			
			BluetoothService.Teleop_run("setMaxVel", Teleop_Vel);
			BluetoothService.Teleop_run("setMaxRotVel", ((Teleop_Vel/10)*Teleop_RotVel_unit)/10);
			
			
			setvadd.setOnTouchListener(this);
			setvsub.setOnTouchListener(this);
			
			up.setOnTouchListener(this);
			down.setOnTouchListener(this);
			left.setOnTouchListener(this);
			right.setOnTouchListener(this);
			
			if( timer==null ){
				timer = new Timer();
				timer.scheduleAtFixedRate(new updataVelText(), 0, 300);
			}
		}
		return InputFragmentView;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if( v.getId()==R.id.teleop_up ){
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				BluetoothService.Teleop_run("Trans", Teleop_Vel);
			}
			else if(event.getAction()==MotionEvent.ACTION_UP){
				BluetoothService.Teleop_run("Trans", 0);
			}
		}
		else if( v.getId()==R.id.teleop_down ){
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				BluetoothService.Teleop_run("Trans", (-1)*Teleop_Vel);
			}
			else if(event.getAction()==MotionEvent.ACTION_UP){
				BluetoothService.Teleop_run("Trans", 0);
			}
		}
		else if( v.getId()==R.id.teleop_left ){
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				BluetoothService.Teleop_run("Rotate", ((Teleop_Vel/10)*Teleop_RotVel_unit)/10);
			}
			else if(event.getAction()==MotionEvent.ACTION_UP){
				BluetoothService.Teleop_run("Rotate", 0);
			}
		}
		else if( v.getId()==R.id.teleop_right ){
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				BluetoothService.Teleop_run("Rotate", ((-1)*(Teleop_Vel/10)*Teleop_RotVel_unit)/10);
			}
			else if(event.getAction()==MotionEvent.ACTION_UP){
				BluetoothService.Teleop_run("Rotate", 0);
			}
		}
		else if( v.getId()==R.id.setvadd ){
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				isNeedAdd = true;
                new Thread(addRunnable).start();
			}
			else if(event.getAction()==MotionEvent.ACTION_UP){
				isNeedAdd = false;
			}
		}
		else if( v.getId()==R.id.setvsub ){
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				isNeedSub = true;
				new Thread(subRunnable).start();
			}
			else if(event.getAction()==MotionEvent.ACTION_UP){
				isNeedSub = false;
			}
		}
		return false;
	}
	
	private Runnable addRunnable = new Runnable() {
        @Override
        public void run() {
                while( isNeedAdd ){
                        uiHandler.sendEmptyMessage(0);
                        try {
                                Thread.sleep(500);
                        } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        }
                }
        }
	};
	
	private Runnable subRunnable = new Runnable() {
        @Override
        public void run() {
                while( isNeedSub ){
                	uiHandler.sendEmptyMessage(0);
                    try {
                            Thread.sleep(500);
                    } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                    }
                }
        }
	};
	
	private Handler uiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
        	if( isNeedAdd ){
        		if( Teleop_Vel<1000 ) Teleop_Vel += 10;
    			
    			if( Teleop_Vel ==1000 ) setvtxt.setTextSize(13);
    			else if( Teleop_Vel>=100 ) setvtxt.setTextSize(16);
    			else setvtxt.setTextSize(25);
    			
    			BluetoothService.Teleop_setv = String.valueOf(Teleop_Vel);
    			setvtxt.setText(BluetoothService.Teleop_setv);
    			
    			BluetoothService.Teleop_run("setMaxVel", Teleop_Vel);
    			BluetoothService.Teleop_run("setMaxRotVel", ((Teleop_Vel/10)*Teleop_RotVel_unit)/10);
        	}
        	else if( isNeedSub ){
        		if( Teleop_Vel>=10 ) Teleop_Vel -= 10;
    			
        		BluetoothService.Teleop_setv = String.valueOf(Teleop_Vel);
    			setvtxt.setText(BluetoothService.Teleop_setv);
    			if( Teleop_Vel>=100 ) setvtxt.setTextSize(16);
    			else setvtxt.setTextSize(25);
    			
    			BluetoothService.Teleop_run("setMaxVel", Teleop_Vel);
    			BluetoothService.Teleop_run("setMaxRotVel", ((Teleop_Vel/10)*Teleop_RotVel_unit)/10);
        	}
            super.handleMessage(msg);
        }
        
	};
	
	public void setVelText(){
		int vel = (int)BluetoothService.Amigo.getVel();
		if( vel>60000 ){
			vel = 65535-vel;
		}
		else if( vel>30000 ){
			vel = (int)BluetoothService.Amigo.getRightVel();
			if( vel>60000 ) vel = 65535-vel;
		}
		veltxt.setText(String.valueOf(vel));
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		if( timer==null && BluetoothService.mAmigoState.equals(AmigoState.running) 
				&& !AmigoCommunication.WanderModeStatus
				&& !MonitorService.MonitorMode ){
			timer = new Timer();
			timer.scheduleAtFixedRate(new updataVelText(), 0, 300);
		}
		super.onResume();
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		if( timer!=null && BluetoothService.mAmigoState.equals(AmigoState.running) ){
			timer.cancel();
			timer = null;
		}
		super.onStop();
	}
	
	class updataVelText extends TimerTask {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					setVelText();
				}
			});
		}
	}
	
}
