package com.control.amigo;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;


public class MonitorFWManager {
	
	private static MonitorFWView previewWindow;
	private static LayoutParams previewWindowParams;
	private static WindowManager mWindowManager;
	
	public static void createPreviewWindow(Context context) {
		WindowManager windowManager = getWindowManager(context);
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		if (previewWindow == null) {
			previewWindow = new MonitorFWView(context);
			if (previewWindowParams == null) {
				previewWindowParams = new LayoutParams();
				previewWindowParams.x = screenWidth;
				previewWindowParams.y = screenHeight;
				previewWindowParams.type = LayoutParams.TYPE_PHONE;
				previewWindowParams.format = PixelFormat.RGBA_8888;
				previewWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
						| LayoutParams.FLAG_NOT_FOCUSABLE;
				previewWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
				previewWindowParams.width = 1;
				previewWindowParams.height = 1;
			}
			previewWindow.setParams(previewWindowParams);
			windowManager.addView(previewWindow, previewWindowParams);
		}
		previewWindow.CamInit();
	}
	
	public static void removePreviewWindow(Context context) {
		if (previewWindow != null) {
			WindowManager windowManager = getWindowManager(context);
			windowManager.removeView(previewWindow);
			previewWindow = null;
			previewWindowParams = null;
		}
	}
	
	public static boolean isWindowShowing() {
		return previewWindow != null;
	}
	
	private static WindowManager getWindowManager(Context context) {
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		}
		return mWindowManager;
	}
	
	public static void changeBigPreviewWindow(Context context){
		WindowManager windowManager = getWindowManager(context);
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		if (previewWindow != null) {
			if (previewWindowParams != null) {
				previewWindowParams.x = screenWidth;
				previewWindowParams.y = screenHeight/5;
				previewWindowParams.type = LayoutParams.TYPE_PHONE;
				previewWindowParams.format = PixelFormat.RGBA_8888;
				previewWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
						| LayoutParams.FLAG_NOT_FOCUSABLE;
				previewWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
				previewWindowParams.width = MonitorFWView.viewWidth;
				previewWindowParams.height = MonitorFWView.viewHeight;
			}
			previewWindow.updateViewLayoutSize(previewWindowParams);
		}
	}
	
	public static void changeSmallPreviewWindow(Context context){
		WindowManager windowManager = getWindowManager(context);
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		if (previewWindow != null) {
			if (previewWindowParams != null) {
				previewWindowParams.x = screenWidth;
				previewWindowParams.y = screenHeight;
				previewWindowParams.type = LayoutParams.TYPE_PHONE;
				previewWindowParams.format = PixelFormat.RGBA_8888;
				previewWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
						| LayoutParams.FLAG_NOT_FOCUSABLE;
				previewWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
				previewWindowParams.width = 1;
				previewWindowParams.height = 1;
			}
			previewWindow.updateViewLayoutSize(previewWindowParams);
		}
	}
	
}