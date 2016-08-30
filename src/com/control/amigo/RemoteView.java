package com.control.amigo;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;

import com.example.amigo.R;

public class RemoteView extends Fragment implements Runnable, SurfaceHolder.Callback{
	private SurfaceView mSurfaceView;
	private Canvas canvas;
	private SurfaceHolder mholder;
	private Bitmap bitmap;
	private boolean Run = false;
	
	private int viewHeight;
	private int viewWidth;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.remote_view, container, false);
		
		mSurfaceView = (SurfaceView) view.findViewById(R.id.surfaceView1);
		mholder = mSurfaceView.getHolder();
		mholder.addCallback(this);
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.remotview_connecting, null);
		
		viewWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
		android.view.ViewGroup.LayoutParams params= view.getLayoutParams();
		viewHeight = params.height;
		
		return view;
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Run = false;
		canvas = null;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			while(Run){Log.e("qq", "socket!!!");
				Socket socket = new Socket(MonitorService.serverAddr, 1133);
				
				byte buf[] = new byte[1024];
				byte imgbuf[] = new byte[20000];
				int offset=0,len;
				InputStream inputStream = socket.getInputStream();
				
				while ((len = inputStream.read(buf)) != -1) {Log.e("qq", len+"----"+offset);
					for( int i=0; i<len; ++i ){
						imgbuf[offset++] = buf[i];
					}
				}
				
				ByteArrayOutputStream outPut = new ByteArrayOutputStream();
                bitmap = BitmapFactory.decodeByteArray(imgbuf, 0, offset);
                
                if( bitmap!=null && Run ){
                	bitmap.compress(CompressFormat.JPEG, 80, outPut);
                	canvas = mholder.lockCanvas();
    				canvas.drawBitmap(bitmap, (viewWidth-bitmap.getWidth())/2, (viewHeight-bitmap.getHeight())/2, null);
    				mholder.unlockCanvasAndPost(canvas);
                }
				inputStream.close();
                socket.close();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		canvas = mholder.lockCanvas();
		canvas.drawBitmap(bitmap, (viewWidth-bitmap.getWidth())/2, (viewHeight-bitmap.getHeight())/2, null);
		mholder.unlockCanvasAndPost(canvas);
		
		Run = true;
		new Thread(this).start();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}
	
}
