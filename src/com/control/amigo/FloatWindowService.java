package com.control.amigo;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;

public class FloatWindowService extends Service {

	private Handler handler = new Handler();

	private Timer timer;
	public static boolean display = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (timer == null) {
			timer = new Timer();
			timer.scheduleAtFixedRate(new RefreshTask(), 0, 100);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		timer.cancel();
		timer = null;
	}

	class RefreshTask extends TimerTask {

		@Override
		public void run() {
			if (!FloatWindowManager.isWindowShowing() && 
					BluetoothService.mAmigoState.equals(BluetoothService.AmigoState.running) && display ) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						FloatWindowManager.createSmallWindow(getApplicationContext());
					}
				});
			}
			else if( FloatWindowManager.isWindowShowing() &&
					BluetoothService.mAmigoState.equals(BluetoothService.AmigoState.stopped) || !display ){
				FloatWindowManager.removeBigWindow(getApplicationContext());
				FloatWindowManager.removeSmallWindow(getApplicationContext());
			}
			else if( FloatWindowManager.isWindowShowing()&& 
					BluetoothService.mAmigoState.equals(BluetoothService.AmigoState.running) && display ) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						FloatWindowManager.updateAmigoInfo();
					}
				});
			}
		}

	}

	private boolean isHome() {
		ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
		return getHomes().contains(rti.get(0).topActivity.getPackageName());
	}

	private List<String> getHomes() {
		List<String> names = new ArrayList<String>();
		PackageManager packageManager = this.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		for (ResolveInfo ri : resolveInfo) {
			names.add(ri.activityInfo.packageName);
		}
		return names;
	}
}
