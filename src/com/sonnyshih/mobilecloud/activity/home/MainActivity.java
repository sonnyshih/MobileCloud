package com.sonnyshih.mobilecloud.activity.home;

import com.sonnyshih.mobilecloud.R;
import com.sonnyshih.mobilecloud.base.BaseFragmentActivity;
import com.sonnyshih.mobilecloud.manage.ApplicationManager;
import com.sonnyshih.mobilecloud.manage.BonjourManage;
import com.sonnyshih.mobilecloud.manage.BonjourManage.BonjourHanlder;
import com.sonnyshih.mobilecloud.util.StringUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

public class MainActivity extends BaseFragmentActivity implements BonjourHanlder{

	private AlertDialog retryAlertDialog;
	private BonjourManage bonjourManage;
	private boolean isGetDriveIp = false;
	
	private RelativeLayout relativeLayout;
	private RelativeLayout imageLayout;

	// private TextView textView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		relativeLayout = (RelativeLayout) findViewById(R.id.start_relativeLayout);
		imageLayout = (RelativeLayout) findViewById(R.id.start_imageLayout);
		// textView = (TextView) findViewById(R.id.main_textView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		showProgressDialog("Waiting","Discovering the Mobile Cloud...");
		isGetDriveIp = false;
		bonjourManage = new BonjourManage(this);
		bonjourManage.startBonjour();
		waitingForBonjourData();
	}

	// Waiting for 3 seconds. 
	// If not get bonjour message, close the progress dialog and open the retry dialog.
	private void waitingForBonjourData(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(3000);
					if (!isGetDriveIp) {
						handleNoGetBonjourData();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}).start();

	}
	
	private void handleNoGetBonjourData(){
		
		runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
				dismissProgressDialog();
				showRetryAlertDialog("No Get the Bonjour Data.\nPlease try it again.");
		    }
		});

	}

	private AlertDialog showRetryAlertDialog(String message) {

		if (retryAlertDialog == null) {
			retryAlertDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.app_name)
					.setIcon(android.R.drawable.ic_menu_info_details)
					.setPositiveButton(R.string.retry,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									
									showProgressDialog("Waiting","Discovering the Mobile Cloud...");
									isGetDriveIp = false;
									bonjourManage.startBonjour();
									waitingForBonjourData();
								}
							}).setCancelable(false).create();
		}

		if (retryAlertDialog.isShowing()) {
			return retryAlertDialog;
		}

		retryAlertDialog.setMessage(message);
		retryAlertDialog.show();
		return retryAlertDialog;
	}

	
	@Override
	public void doRouterExcute(String routerIp, String routerPort, String routerMac) {
		Log.d("Mylog", "routerIp="+routerIp);
	
		if (!StringUtil.isEmpty(routerIp)) {
			ApplicationManager.getInstance().setRouterIp(routerIp);
			ApplicationManager.getInstance().setRouterPort(routerPort);
			ApplicationManager.getInstance().setRouterMac(routerMac);
		}
	}

	@Override
	public void doDriveExcute(String driveIp, String drivePort, String driveMac) {
		Log.d("Mylog", "driveIp="+driveIp);

		if (!StringUtil.isEmpty(driveIp)) {
			ApplicationManager.getInstance().setDriveIp(driveIp);
			ApplicationManager.getInstance().setDrivePort(drivePort);
			ApplicationManager.getInstance().setDriveMac(driveMac);
			
			isGetDriveIp = true;
			dismissProgressDialog();
		}

	}

	@Override
	public void doErrorExcute(String errorMessage) {
		
	}
	
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.main, menu);
	// return true;
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// // Handle action bar item clicks here. The action bar will
	// // automatically handle clicks on the Home/Up button, so long
	// // as you specify a parent activity in AndroidManifest.xml.
	// int id = item.getItemId();
	// if (id == R.id.action_settings) {
	// return true;
	// }
	// return super.onOptionsItemSelected(item);
	// }
}
