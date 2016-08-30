package com.control.amigo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.control.amigo.BluetoothService.AmigoState;
import com.control.amigo.BluetoothService.BTState;
import com.control.amigo.MonitorService.PreviewStatus;
import com.control.amigo.MonitorFWManager;
import com.example.amigo.R;

public class FragtabsActivity extends FragmentActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener, OnClickListener {
	private TabHost mTabHost;
	private HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
	private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private boolean isExit = false, pagechange = false, tabchange = false, switchInit = false, remoteviewSwitch = false;
    private DrawerLayout layDrawer;
    private ActionBarDrawerToggle drawerToggle;
	private ListView lstDrawer;
	private String[] drawer_menu = new String[]{ "AmigoInfo", "MonitorView" ,"ServerIP修改" };
	private drawerAdapter drawerAdapter;
	private int CurrentPage = 0;
	public static String ServerIP = "120.105.129.101";
	private Fragment remoteView;
	private ImageButton remoteViewbtn;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.fragment_tabs);
        
        initActionBar();
        initDrawer();
        initTabHost();
        initViewPager();
        
        Intent floatwindowIntent = new Intent();
		floatwindowIntent.setClass(this, FloatWindowService.class);
		startService(floatwindowIntent);
		
		Intent monitorIntent = new Intent(MonitorService.ACTION_MONITOR_WORK);
		startService(monitorIntent);
		
		InitServerIP();
    }
    
    private void initActionBar(){
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,Gravity.CENTER);
        View viewTitleBar = getLayoutInflater().inflate(R.layout.actionbar_view, null);
        getActionBar().setCustomView(viewTitleBar, lp);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setDisplayShowCustomEnabled(true);
        
        TextView title = (TextView)getActionBar().getCustomView().findViewById(R.id.title);
        title.setText("Amigo");
        title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/ARBERKLEY.TTF"));
        
        ImageButton menuImgBtn = (ImageButton)getActionBar().getCustomView().findViewById(R.id.menubtn);
        remoteViewbtn = (ImageButton)getActionBar().getCustomView().findViewById(R.id.remoteViewbtn);
        menuImgBtn.setOnClickListener(this);
        remoteViewbtn.setOnClickListener(this);
    }
    
    private void initDrawer(){
    	layDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		lstDrawer = (ListView) findViewById(R.id.left_drawer);
		
		layDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		drawerToggle = new ActionBarDrawerToggle(
				this, 
				layDrawer,
				R.drawable.menu_buttton, 
				R.string.drawer_open,
				R.string.drawer_close) {

			@Override
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				TextView title = (TextView)getActionBar().getCustomView().findViewById(R.id.title);
		        title.setText("Amigo");
		        title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/ARBERKLEY.TTF"));
		        
		        ImageButton menuImgBtn = (ImageButton)getActionBar().getCustomView().findViewById(R.id.menubtn);
		        menuImgBtn.setImageResource(R.drawable.menu_buttton);
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				TextView title = (TextView)getActionBar().getCustomView().findViewById(R.id.title);
		        title.setText("Menu");
		        title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/ARBERKLEY.TTF"));
		        
		        ImageButton menuImgBtn = (ImageButton)getActionBar().getCustomView().findViewById(R.id.menubtn);
		        menuImgBtn.setImageResource(R.drawable.menu_buttton2);
		        
		        drawerAdapter.notifyDataSetChanged();
			}
		};
		drawerToggle.syncState();
		
		layDrawer.setDrawerListener(drawerToggle);
		
		drawerAdapter = new drawerAdapter(this, drawer_menu);
		lstDrawer.setAdapter(drawerAdapter);
		
		lstDrawer.setOnItemClickListener(new DrawerItemClickListener());
    }
    
    private void initTabHost(){
        mTabHost = (TabHost)findViewById(R.id.tabhost);
        mTabHost.setup();
        
        View tab1 = LayoutInflater.from(this).inflate(R.layout.tab_view, null);
        TextView text1 = (TextView) tab1.findViewById(R.id.tabtext);
        text1.setText("Connect");
        addTab(mTabHost.newTabSpec("Connect").setIndicator(tab1),
        		Connect.class, null);
        View tab2 = LayoutInflater.from(this).inflate(R.layout.tab_view, null);
        TextView text2 = (TextView) tab2.findViewById(R.id.tabtext);
        text2.setText("Teleop");
        addTab(mTabHost.newTabSpec("Teleop").setIndicator(tab2),
        		Teleop.class, null);
        View tab3 = LayoutInflater.from(this).inflate(R.layout.tab_view, null);
        TextView text3 = (TextView) tab3.findViewById(R.id.tabtext);
        text3.setText("Wander");
        addTab(mTabHost.newTabSpec("Wander").setIndicator(tab3),
        		Wander.class, null);
        View tab4 = LayoutInflater.from(this).inflate(R.layout.tab_view, null);
        TextView text4 = (TextView) tab4.findViewById(R.id.tabtext);
        text4.setText("Monitor");
        addTab(mTabHost.newTabSpec("Monitor").setIndicator(tab4),
        		Monitor.class, null);
        
        mTabHost.setCurrentTab(0);
		   
        DisplayMetrics dm = new DisplayMetrics();   
        getWindowManager().getDefaultDisplay().getMetrics(dm); 
        int screenWidth = dm.widthPixels;
        TabWidget tabWidget = mTabHost.getTabWidget(); 
        int count = tabWidget.getChildCount(); 
        if (count > 3) { 
            for (int i = 0; i < count; i++) {   
                tabWidget.getChildTabViewAt(i).setMinimumWidth((screenWidth) / 4);
            }   
        }
        
        mTabHost.setOnTabChangedListener(this);
    }
    
    private void initViewPager(){
    	ArrayList<Fragment> fragments = new ArrayList<Fragment>();
		fragments.add(Fragment.instantiate(this, Connect.class.getName()));
		fragments.add(new Fragment());
		fragments.add(Fragment.instantiate(this, Teleop.class.getName()));
		fragments.add(new Fragment());
		fragments.add(Fragment.instantiate(this, Wander.class.getName()));
		fragments.add(new Fragment());
		fragments.add(Fragment.instantiate(this, Monitor.class.getName()));
		mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), fragments);
		mViewPager = (ViewPager) findViewById(R.id.realtabcontent);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(this);
    }
    
    private void InitServerIP(){
    	String filename = "ServerIP.txt";
		File inFile = new File(getFilesDir(), filename);
		if( !inFile.exists() ){
			try {
				inFile.createNewFile();
				FileWriter fileOut = new FileWriter(inFile);
				fileOut.write(ServerIP);
				fileOut.flush();
				fileOut.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			try {
				FileReader fileIn = new FileReader(inFile);
				BufferedReader fileInBuffer = new BufferedReader(fileIn);
				ServerIP = fileInBuffer.readLine();
				fileInBuffer.close();
				fileIn.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }

	@Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    }
    
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if( BluetoothService.mBTState.equals(BTState.running) ){
			if( BluetoothService.mAmigoState.equals(AmigoState.running) ){
				BluetoothService.AmigoSwitch();
			}
			Intent intentBluetoothStop = new Intent(BluetoothService.ACTION_STOP);
			startService(intentBluetoothStop);
		}
		
		Intent intentBluetooth = new Intent(this, BluetoothService.class);
		stopService(intentBluetooth);
		
		Intent intentFloatWindow = new Intent(this, FloatWindowService.class);
		stopService(intentFloatWindow);
		
		if( MonitorService.mWifiPosStatus.equals(MonitorService.WifiPosStatus.wificonnected) ){
			Intent monitorIntent = new Intent(MonitorService.ACTION_MONITOR_WIFIPOS_STOP);
			startService(monitorIntent);
		}
		
		Intent monitorIntent = new Intent(this, MonitorService.class);
		stopService(monitorIntent);
		
		System.exit(0);
		
		super.onDestroy();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if( keyCode == KeyEvent.KEYCODE_BACK ){
			exitBy2Click();
		}
		return false;
	}
	
	private void exitBy2Click() {  
	    Timer tExit = null;  
	    if (isExit == false) {  
	        isExit = true;  
	        Toast.makeText(this, "再按一次退出程式", Toast.LENGTH_SHORT).show();  
	        tExit = new Timer();  
	        tExit.schedule(new TimerTask() {  
	            @Override  
	            public void run() {  
	                isExit = false;
	            }  
	        }, 2000);
	  
	    } else {
	    	finish();
	    }  
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			switch( position ){
				case 0:
					break;
				case 1:
					break;
				case 2:
					DialogFragment dialog = new ModifyIPDialog();
					dialog.show(getSupportFragmentManager(), "dialog");
					layDrawer.closeDrawer(lstDrawer);
					break;
				default:
					break;
			}
		}
		
	}
	
    static final class TabInfo {
        private final String tag;
        private final Class<?> clss;
        private final Bundle args;
        private Fragment fragment;

        TabInfo(String _tag, Class<?> _class, Bundle _args) {
            tag = _tag;
            clss = _class;
            args = _args;
        }
    }

    static class DummyTabFactory implements TabHost.TabContentFactory {
        private final Context mContext;

        public DummyTabFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }
    
    public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
        tabSpec.setContent(new DummyTabFactory(this));
        mTabHost.addTab(tabSpec);
        
        String tag = tabSpec.getTag();
        TabInfo info = new TabInfo(tag, clss, args);
        mTabs.put(tag, info);
    }
	
	public class PagerAdapter extends FragmentPagerAdapter {
		 
	    private List<Fragment> fragments;
	    
	    public PagerAdapter(FragmentManager fragmentmanager, List<Fragment> fragments) {
	        super(fragmentmanager);
	        this.fragments = fragments;
	    }
	    
		@Override
	    public Fragment getItem(int position) {
	        return this.fragments.get(position);
	    }
	    
		@Override
	    public int getCount() {
	        return this.fragments.size();
	    }
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		if( position-CurrentPage>0 && !tabchange ){
			if( !pagechange ){
				pagechange = true;
				mViewPager.setCurrentItem(position+1);
				mTabHost.setCurrentTab((position+1)/2);
				CurrentPage = position+1;
				pagechange = false;
			}
		}
		else if( position-CurrentPage<0 && !tabchange ){
			if( !pagechange ){
				pagechange = true;
				mViewPager.setCurrentItem(position-1);
				mTabHost.setCurrentTab((position-1)/2);
				CurrentPage = position-1;
				pagechange = false;
			}
		}
		else if( tabchange ){
			CurrentPage = position;
		}
	}

	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		if( !pagechange ){
			tabchange = true;
			int position = mTabHost.getCurrentTab();
			mViewPager.setCurrentItem(position*2);
			tabchange = false;
		}
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if( v.getId()==R.id.menubtn ){
			if( !layDrawer.isDrawerOpen(lstDrawer) ){
				layDrawer.openDrawer(lstDrawer);
			}
			else if( layDrawer.isDrawerOpen(lstDrawer) ){
				layDrawer.closeDrawer(lstDrawer);
			}
		}
		else if( v.getId()==R.id.remoteViewbtn ){
			if( remoteviewSwitch == false ){
				remoteView = new RemoteView();
				getSupportFragmentManager().beginTransaction().replace(R.id.tabhost, remoteView).commit();
				remoteviewSwitch = true;
			}
			else if( remoteviewSwitch == true ){
				getSupportFragmentManager().beginTransaction().remove(remoteView).commit();
				remoteviewSwitch = false;
			}
		}
	}
	
	private class drawerAdapter extends BaseAdapter {
		private LayoutInflater myInflater;
		private String[] arr;
		private Switch drawerSwitch;
		
		public drawerAdapter( Context c, String[] arr ) {
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
			convertView = myInflater.inflate(R.layout.menudrawer_listview, null);
			drawerSwitch = (Switch) convertView.findViewById(R.id.drawer_switch);
			TextView drawertext = (TextView) convertView.findViewById(R.id.drawer_listtxt);
			switchInit = true;
			
			switch( position ){
				case 0:
					drawertext.setText(arr[position]);
					drawerSwitch.setOnCheckedChangeListener(drawerSwitchlst1);
					
					if( BluetoothService.mAmigoState.equals(BluetoothService.AmigoState.running) ){
			        	if( FloatWindowService.display ){
				        	drawerSwitch.setChecked(true);
				        }
				        else if( !FloatWindowService.display ){
				        	drawerSwitch.setChecked(false);
				        }
			        }
					else {
						drawerSwitch.setChecked(false);
					}
					break;
				case 1:
					drawertext.setText(arr[position]);
					drawerSwitch.setOnCheckedChangeListener(drawerSwitchlst2);
					
					if( MonitorService.mPreviewStatus.equals(PreviewStatus.opened) ){
			        	if( !MonitorService.PreViewSize ){
			        		drawerSwitch.setChecked(false);
			        	}
			        	else if( MonitorService.PreViewSize ){
			        		drawerSwitch.setChecked(true);
			        	}
			        }
					else{
						drawerSwitch.setChecked(false);
					}
					break;
				case 2:
					drawerSwitch.setVisibility(View.GONE);
					drawertext.setText(arr[position]);
					FrameLayout.LayoutParams param = new FrameLayout.LayoutParams
							(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
							 android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
					param.gravity = Gravity.CENTER_HORIZONTAL;
					drawertext.setLayoutParams(param);
					break;
				
			}
			switchInit = false;
			return convertView;
		}
		
	}
	
	private OnCheckedChangeListener drawerSwitchlst1 = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			if( !switchInit ){
				if( BluetoothService.mAmigoState.equals(BluetoothService.AmigoState.running) ){
					if( FloatWindowService.display ){
						FloatWindowService.display = false;
						buttonView.setChecked(false);
					}
					else if( !FloatWindowService.display ){
						FloatWindowService.display = true;
						buttonView.setChecked(true);
					}
		        }
				else{
					Toast.makeText(FragtabsActivity.this, "Amigo尚未連線", Toast.LENGTH_SHORT).show();
					drawerAdapter.notifyDataSetChanged();
					layDrawer.closeDrawer(lstDrawer);
				}
			}
		}
	};
	
	private OnCheckedChangeListener drawerSwitchlst2 = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			if( !switchInit ){
				if( MonitorService.mPreviewStatus.equals(PreviewStatus.opened) ){
		        	if( !MonitorService.PreViewSize ){
		        		Handler FWviewhandler = new Handler();
		        		FWviewhandler.post(new Runnable() {
	        				
	        				@Override
	        				public void run() {
	        					// TODO Auto-generated method stub
	        					MonitorFWManager.changeBigPreviewWindow(getApplicationContext());
	        					MonitorService.PreViewSize = true;
	        				}
	        			});
		        	}
		        	else if( MonitorService.PreViewSize ){
		        		Handler FWviewhandler = new Handler();
		        		FWviewhandler.post(new Runnable() {
	        				
	        				@Override
	        				public void run() {
	        					// TODO Auto-generated method stub
	        					MonitorFWManager.changeSmallPreviewWindow(getApplicationContext());
	        					MonitorService.PreViewSize = false;
	        				}
	        			});
		        	}
		        }
				else{
					Toast.makeText(FragtabsActivity.this, "MobileCam未開啟", Toast.LENGTH_SHORT).show();
					drawerAdapter.notifyDataSetChanged();
					layDrawer.closeDrawer(lstDrawer);
				}
			}
		}
	};
	
}
