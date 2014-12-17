package com.sonnyshih.mobilecloud.activity.home;

import java.util.ArrayList;
import java.util.List;

import com.sonnyshih.mobilecloud.R;
import com.sonnyshih.mobilecloud.base.BaseFragmentActivity;
import com.sonnyshih.mobilecloud.fragment.home.CloudFragment;
import com.sonnyshih.mobilecloud.fragment.home.AppleFragment;
import com.sonnyshih.mobilecloud.fragment.home.DummyTabContent;
import com.sonnyshih.mobilecloud.ui.adapter.PagerAdapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

public class MainActivity extends BaseFragmentActivity implements
		OnTabChangeListener, OnPageChangeListener {

	private CloudFragment cloudFragment;
	private AppleFragment appleFragment;
	
	private TabHost tabHost;
	private ViewPager viewPager;
	private PagerAdapter pagerAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		
		initTabHost();
		initViewPager();

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	private void initTabHost() {
		tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost.setup();

		setupTab("Cloud", R.drawable.main_tab_icon_cloud);

		setupTab("Device", R.drawable.main_tab_icon_device);

		tabHost.setOnTabChangedListener(this);

	}
	
	
	private void setupTab(String tabName, Integer iconId) {

		View tabView = LayoutInflater.from(this).inflate(
				R.layout.main_tab_item, null);
		
		TextView textView = (TextView) tabView
				.findViewById(R.id.main_tabTextView);
		textView.setText(tabName);
		
		ImageView imageView = (ImageView) tabView
				.findViewById(R.id.main_tabImageView);
		imageView.setImageResource(iconId);

		TabSpec setContent = tabHost.newTabSpec(tabName).setIndicator(tabView)
				.setContent(new DummyTabContent(this));
		tabHost.addTab(setContent);

	}

	
	private void initViewPager() {

		List<Fragment> fragmentList = new ArrayList<Fragment>();
		
		cloudFragment = new CloudFragment();
		appleFragment = new AppleFragment();
		fragmentList.add(cloudFragment);
		fragmentList.add(appleFragment);

		pagerAdapter = new PagerAdapter(getSupportFragmentManager(),
				fragmentList);

		viewPager = (ViewPager) findViewById(R.id.main_viewpager);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setOnPageChangeListener(this);
	}

	@Override
	public void onTabChanged(String tag) {
		int position = tabHost.getCurrentTab();
		viewPager.setCurrentItem(position);
		cloudFragment.closeActionMode();
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {
		tabHost.setCurrentTab(position);
		cloudFragment.closeActionMode();
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}
	
	// Waiting for 3 seconds. 
	// If not get bonjour message, close the progress dialog and open the retry dialog.
//	private void waitingForBonjourData(){
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					Thread.sleep(3000);
//					if (!isGetDriveIp) {
//						handleNoGetBonjourData();
//					}
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				
//			}
//		}).start();
//
//	}
	
//	private void handleNoGetBonjourData(){
//		
//		runOnUiThread(new Runnable() {
//		    @Override
//		    public void run() {
//				dismissProgressDialog();
//				showRetryAlertDialog("No Get the Bonjour Data.\nPlease try it again.");
//		    }
//		});
//
//	}
	
}
