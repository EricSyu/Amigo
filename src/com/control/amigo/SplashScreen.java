package com.control.amigo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import com.example.amigo.R;

public class SplashScreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splashscreen_view);
		
		TextView txt = (TextView)findViewById(R.id.splashtxt);
		txt.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/ARBERKLEY.TTF"));
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(3000);
					startActivity(new Intent().setClass(SplashScreen.this, FragtabsActivity.class));
					finish();
					overridePendingTransition(R.animator.in_from_right, R.animator.out_to_left);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
}
