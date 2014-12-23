package com.sonnyshih.mobilecloud.manage;

import java.io.File;

import com.sonnyshih.mobilecloud.util.FileUtil;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class ApplicationManager {

	private static ApplicationManager instance;
	private String localIp;

	private String routerMobileCloudName;
	private String routerIp;
	private String routerPort;
	private String routerMac;

	private String driveMobileCloudName;
	private String driveIp;
	private String drivePort;
	private String driveMac;

	private String webDavUsername = "admin"; // webDav Username
	private String webDavPassword = "admin"; // WebDave Password

	private Context context;
	private String appVersion;

	private ActivityManager activityManager;
	private String DOWNLOAD_FOLDER_NAME = "Mobile Cloud Download";
	
	private ApplicationManager() {
	}

	public static ApplicationManager getInstance() {
		if (instance == null) {
			instance = new ApplicationManager();
		}
		return instance;
	}

	public void createDownloadFolder(){
		File folder = new File(FileUtil.getStorageRootPath() + "/" + DOWNLOAD_FOLDER_NAME);
		if (!folder.exists()) {
			folder.mkdir();
		}
	}
	
	public String getDownloadFolderPath(){
		return FileUtil.getStorageRootPath() + "/" + DOWNLOAD_FOLDER_NAME;
	}
	
	public ActivityManager getActivityManager() {
		return activityManager;
	}

	public void setActivityManager(ActivityManager activityManager) {
		this.activityManager = activityManager;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getLocalIp() {
		return localIp;
	}

	public void setLocalIp(String localIp) {
		this.localIp = localIp;
	}

	public String getRouterMobileCloudName() {
		return routerMobileCloudName;
	}

	public void setRouterMobileCloudName(String routerMobileCloudName) {
		this.routerMobileCloudName = routerMobileCloudName;
	}

	public String getRouterIp() {
		return routerIp;
	}

	public void setRouterIp(String routerIp) {
		this.routerIp = routerIp;
	}

	public String getRouterPort() {
		return routerPort;
	}

	public void setRouterPort(String routerPort) {
		this.routerPort = routerPort;
	}

	public String getRouterMac() {
		return routerMac;
	}

	public void setRouterMac(String routerMac) {
		this.routerMac = routerMac;
	}

	public String getDriveMobileCloudName() {
		return driveMobileCloudName;
	}

	public void setDriveMobileCloudName(String driveMobileCloudName) {
		this.driveMobileCloudName = driveMobileCloudName;
	}

	public String getDriveIp() {
		return driveIp;
	}

	public void setDriveIp(String driveIp) {
		this.driveIp = driveIp;
	}

	public String getDrivePort() {
		return drivePort;
	}

	public void setDrivePort(String drivePort) {
		this.drivePort = drivePort;
	}

	public String getDriveMac() {
		return driveMac;
	}

	public void setDriveMac(String driveMac) {
		this.driveMac = driveMac;
	}

	public String getWebDavUsername() {
		return webDavUsername;
	}

	public void setWebDavUsername(String webDavUsername) {
		this.webDavUsername = webDavUsername;
	}

	public String getWebDavPassword() {
		return webDavPassword;
	}

	public void setWebDavPassword(String webDavPassword) {
		this.webDavPassword = webDavPassword;
	}

	public boolean isWifiEnable() {
		boolean iswifiEnable = false;

		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		int intAddress = wifiManager.getConnectionInfo().getIpAddress();

		if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED
				&& intAddress != 0) {

			iswifiEnable = true;
		}

		return iswifiEnable;
	}

	public String getIpAddress() {

		String ip = "";

		if (isWifiEnable()) {

			WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);

			WifiInfo wifiInfo = wifiManager.getConnectionInfo();

			int ipAddress = wifiInfo.getIpAddress();

			ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
					(ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
					(ipAddress >> 24 & 0xff));
		}

		return ip;
	}

	public boolean isServiceRunning(String className) {
		for (RunningServiceInfo service : activityManager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (className.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
