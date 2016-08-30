package com.control.amigo;

import android.support.v4.app.FragmentActivity;
import android.widget.TabHost;

public class TabManager implements TabHost.OnTabChangeListener {
    private FragmentActivity mActivity;
    private TabHost mTabHost;
    



    public TabManager(FragmentActivity activity, TabHost tabHost) {
        mActivity = activity;
        mTabHost = tabHost;
    }



    @Override
    public void onTabChanged(String tabId) {
    	int position = mTabHost.getCurrentTab();
		
    }
    
}