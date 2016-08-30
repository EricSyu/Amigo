package com.control.amigo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.control.amigo.MonitorService.CamSendThread;
import com.control.amigo.MonitorService.MobileCamStatus;
import com.example.amigo.R;

public class MonitorFWView extends LinearLayout implements SurfaceHolder.Callback,PreviewCallback {
	
	public static int viewWidth;
	public static int viewHeight;
	private static int statusBarHeight;
	private WindowManager windowManager;
	private WindowManager.LayoutParams mParams;
	private float xInScreen;
	private float yInScreen;
	private float xDownInScreen;	
	private float yDownInScreen;
	private float xInView;
	private float yInView;
	
	private SurfaceView mSurfaceView;
	private Camera camera;
	private SurfaceHolder mSurfaceHolder = null;
	
	public MonitorFWView(final Context context) {
		super(context);
		windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		LayoutInflater.from(context).inflate(R.layout.monitor_preview_view, this);
		View view = findViewById(R.id.preview_view);
		viewWidth = view.getLayoutParams().width;
		viewHeight = view.getLayoutParams().height;
		
		mSurfaceView = (SurfaceView) view.findViewById(R.id.preview_surfaceView);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xInView = event.getX();
			yInView = event.getY();
			xDownInScreen = event.getRawX();
			yDownInScreen = event.getRawY() - getStatusBarHeight();
			xInScreen = event.getRawX();
			yInScreen = event.getRawY() - getStatusBarHeight();
			break;
		case MotionEvent.ACTION_MOVE:
			xInScreen = event.getRawX();
			yInScreen = event.getRawY() - getStatusBarHeight();
			updateViewPosition();
			break;
		default:
			break;
		}
		return true;
	}

	public void setParams(WindowManager.LayoutParams params) {
		mParams = params;
	}
	
	public void updateViewLayoutSize( WindowManager.LayoutParams params ){
		mParams = params;
		windowManager.updateViewLayout(this, mParams);
	}

	private void updateViewPosition() {
		mParams.x = (int) (xInScreen - xInView);
		mParams.y = (int) (yInScreen - yInView);
		windowManager.updateViewLayout(this, mParams);
	}

	private int getStatusBarHeight() {
		if (statusBarHeight == 0) {
			try {
				Class<?> c = Class.forName("com.android.internal.R$dimen");
				Object o = c.newInstance();
				Field field = c.getField("status_bar_height");
				int x = (Integer) field.get(o);
				statusBarHeight = getResources().getDimensionPixelSize(x);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusBarHeight;
	}

	public void CamInit(){
        mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		camera = Camera.open();
		
		if( camera!=null ){
			try {
				Camera.Parameters parameters=camera.getParameters();
				parameters.setPictureFormat(ImageFormat.JPEG);
				parameters.setPreviewSize(320, 240);
				camera.setParameters(parameters);
				camera.setPreviewDisplay(mSurfaceHolder);
				camera.setPreviewCallback(this);
				
				camera.startPreview();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		int mDisplayRotation = 0;
		int mDisplayOrientation = windowManager.getDefaultDisplay().getRotation() * 90;
		CameraInfo mCameraInfo = new CameraInfo();
		Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, mCameraInfo);
		int mCameraOrientation = mCameraInfo.orientation;
		if (mCameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
			mDisplayRotation = (mCameraOrientation - mDisplayOrientation + 360) % 360;
		} else {
			mDisplayRotation = (mCameraOrientation + mDisplayOrientation) % 360;
			mDisplayRotation = (360 - mDisplayRotation) % 360;
		}
		camera.setDisplayOrientation(mDisplayRotation);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if( camera!=null ){
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		if( MonitorService.camRun && MonitorService.sendfirstRun==0 || 
				MonitorService.camRun && MonitorService.mMobileCamStatus.equals(MobileCamStatus.Camconnected) ){
			Size size = camera.getParameters().getPreviewSize();
			try {
	            if(data!=null){
	              YuvImage image = new YuvImage(data,ImageFormat.NV21, size.width, size.height,null);
	              if(image!=null){
	            	  ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	            	  image.compressToJpeg(new Rect(0, 0, size.width, size.height), 85, byteOut);  
	            	  byteOut.flush();
	            	  
	            	  new CamSendThread(byteOut).start();
	            	  MonitorService.sendfirstRun++;
	              }
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
	}
	
}
