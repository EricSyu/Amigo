package com.control.amigo;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;

import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import android.widget.TextView;

import com.example.amigo.R;

public class FloatWindowManager {

	private static FloatWindowSmallView smallWindow;

	private static FloatWindowBigView bigWindow;

	private static LayoutParams smallWindowParams;

	private static LayoutParams bigWindowParams;

	private static WindowManager mWindowManager;

	private static ActivityManager mActivityManager;

	public static void createSmallWindow(Context context) {
		WindowManager windowManager = getWindowManager(context);
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		if (smallWindow == null) {
			smallWindow = new FloatWindowSmallView(context);
			if (smallWindowParams == null) {
				smallWindowParams = new LayoutParams();
				smallWindowParams.type = LayoutParams.TYPE_PHONE;
				smallWindowParams.format = PixelFormat.RGBA_8888;
				smallWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
						| LayoutParams.FLAG_NOT_FOCUSABLE;
				smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
				smallWindowParams.width = FloatWindowSmallView.viewWidth;
				smallWindowParams.height = FloatWindowSmallView.viewHeight;
				smallWindowParams.x = screenWidth;
				smallWindowParams.y = screenHeight / 2;
			}
			smallWindow.setParams(smallWindowParams);
			windowManager.addView(smallWindow, smallWindowParams);
		}
	}

	public static void removeSmallWindow(Context context) {
		if (smallWindow != null) {
			WindowManager windowManager = getWindowManager(context);
			windowManager.removeView(smallWindow);
			smallWindow = null;
			smallWindowParams = null;
		}
	}

	public static void createBigWindow(Context context) {
		WindowManager windowManager = getWindowManager(context);
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		if (bigWindow == null) {
			bigWindow = new FloatWindowBigView(context);
			if (bigWindowParams == null) {
				bigWindowParams = new LayoutParams();
				bigWindowParams.x = screenWidth / 2 - FloatWindowBigView.viewWidth / 2;
				bigWindowParams.y = screenHeight / 2 - FloatWindowBigView.viewHeight / 2;
				bigWindowParams.type = LayoutParams.TYPE_PHONE;
				bigWindowParams.format = PixelFormat.RGBA_8888;
				bigWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
						| LayoutParams.FLAG_NOT_FOCUSABLE;
				bigWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
				bigWindowParams.width = FloatWindowBigView.viewWidth;
				bigWindowParams.height = FloatWindowBigView.viewHeight;
			}
			bigWindow.setParams(bigWindowParams);
			windowManager.addView(bigWindow, bigWindowParams);
		}
	}

	public static void removeBigWindow(Context context) {
		if (bigWindow != null) {
			WindowManager windowManager = getWindowManager(context);
			windowManager.removeView(bigWindow);
			bigWindow = null;
		}
	}

	public static void updateAmigoInfo() {
		if (bigWindow != null) {
			TextView motorView = (TextView) bigWindow.findViewById(R.id.motorStatus);
			TextView batteryView = (TextView) bigWindow.findViewById(R.id.batteryStatus);
			TextView positionView = (TextView) bigWindow.findViewById(R.id.position);
			TextView sonar1View = (TextView) bigWindow.findViewById(R.id.sonar1);
			TextView sonar2View = (TextView) bigWindow.findViewById(R.id.sonar2);
			TextView sonar3View = (TextView) bigWindow.findViewById(R.id.sonar3);
			TextView sonar4View = (TextView) bigWindow.findViewById(R.id.sonar4);
			TextView sonar5View = (TextView) bigWindow.findViewById(R.id.sonar5);
			TextView sonar6View = (TextView) bigWindow.findViewById(R.id.sonar6);
			TextView sonar7View = (TextView) bigWindow.findViewById(R.id.sonar7);
			TextView sonar8View = (TextView) bigWindow.findViewById(R.id.sonar8);
			
			motorView.setText(BluetoothService.Amigo.getMotor());
			batteryView.setText(BluetoothService.Amigo.getBattery());
			positionView.setText(BluetoothService.Amigo.getPosition());
			sonar1View.setText(BluetoothService.Amigo.getSonar1());
			sonar2View.setText(BluetoothService.Amigo.getSonar2());
			sonar3View.setText(BluetoothService.Amigo.getSonar3());
			sonar4View.setText(BluetoothService.Amigo.getSonar4());
			sonar5View.setText(BluetoothService.Amigo.getSonar5());
			sonar6View.setText(BluetoothService.Amigo.getSonar6());
			sonar7View.setText(BluetoothService.Amigo.getSonar7());
			sonar8View.setText(BluetoothService.Amigo.getSonar8());
			
		}
	}

	public static boolean isWindowShowing() {
		return smallWindow != null || bigWindow != null;
	}

	private static WindowManager getWindowManager(Context context) {
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		}
		return mWindowManager;
	}
	
}
