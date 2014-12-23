package com.sonnyshih.mobilecloud.base;

import com.sonnyshih.mobilecloud.R;
import com.sonnyshih.mobilecloud.manage.ApplicationManager;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

public class MobileCloudApp extends Application{

	private static boolean isDebugMode = false;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		setupDebugMode(true);
		
		ApplicationManager.getInstance().createDownloadFolder();
		ApplicationManager.getInstance().setContext(this);
		ApplicationManager.getInstance().setAppVersion(getAppVersion());
		ApplicationManager.getInstance().setActivityManager(
				(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
	}

	private String getAppVersion() {
		return getString(R.string.app_version);
	}
	
	public static boolean isDebugMode() {
		return isDebugMode;
	}

	private void setupDebugMode(boolean isDebug) {
		isDebugMode = isDebug;
	}
	
}
