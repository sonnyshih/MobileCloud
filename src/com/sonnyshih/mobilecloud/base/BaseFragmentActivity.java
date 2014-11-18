package com.sonnyshih.mobilecloud.base;

import com.sonnyshih.mobilecloud.R;
import com.sonnyshih.mobilecloud.manage.ApplicationManager;
import com.sonnyshih.mobilecloud.manage.BonjourManage;
import com.sonnyshih.mobilecloud.manage.BonjourManage.BonjourHanlder;
import com.sonnyshih.mobilecloud.util.StringUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public abstract class BaseFragmentActivity extends FragmentActivity implements BonjourHanlder{
	private BonjourManage bonjourManage;

	private ProgressDialog loadingProgressDialog;
	private AlertDialog errorAlertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	protected void onResume() {
		super.onResume();
		bonjourManage = new BonjourManage(this);
		bonjourManage.startBonjour();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	protected abstract void doRouter(String routerIp, String routerPort,String routerMac);
	protected abstract void doDrive(String driveIp, String drivePort, String driveMac);
	protected abstract void doError(String errorMessage);
	
	protected AlertDialog showErrorAlertDialog(String errorMessage) {

		if (errorAlertDialog == null) {
			errorAlertDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.app_name)
					.setIcon(android.R.drawable.ic_menu_info_details)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).setCancelable(false).create();
		}

		if (errorAlertDialog.isShowing()) {
			return errorAlertDialog;
		}

		errorAlertDialog.setMessage(errorMessage);
		errorAlertDialog.show();
		return errorAlertDialog;
	}

	protected void showProgressDialog(String title, String message) {

		if (loadingProgressDialog == null) {
			loadingProgressDialog = new ProgressDialog(this);
			loadingProgressDialog.setIndeterminate(true);
			loadingProgressDialog.setCanceledOnTouchOutside(false);
		}

		if (title != null && !title.isEmpty()) {
			loadingProgressDialog.setTitle(title);
		}

		if (message != null && !message.isEmpty()) {
			loadingProgressDialog.setMessage(message);
		}

		loadingProgressDialog.show();
	}

	protected void dismissProgressDialog() {
		if (loadingProgressDialog != null && loadingProgressDialog.isShowing()) {
			loadingProgressDialog.dismiss();
		}

	}

	@Override
	public void doRouterExcute(String routerIp, String routerPort, String routerMac) {
		doRouter(routerIp, routerPort, routerMac);
	}

	@Override
	public void doDriveExcute(String driveIp, String drivePort, String driveMac) {
			doDrive(driveIp, drivePort, driveMac);
	}

	@Override
	public void onError(String errorMessage) {
		doError(errorMessage);
	}

}
