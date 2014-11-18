package com.sonnyshih.mobilecloud.activity.home;

import com.sonnyshih.mobilecloud.R;
import com.sonnyshih.mobilecloud.base.BaseFragmentActivity;
import com.sonnyshih.mobilecloud.manage.ApplicationManager;
import com.sonnyshih.mobilecloud.manage.BonjourManage;
import com.sonnyshih.mobilecloud.manage.BonjourManage.BonjourHanlder;
import com.sonnyshih.mobilecloud.ui.animation.Rotate3dAnimation;
import com.sonnyshih.mobilecloud.util.StringUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

public class StartActivity extends BaseFragmentActivity{

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
	}



	private void applyRotation(float start, float end) {
		final float centerX = relativeLayout.getWidth() / 2.0f;
		final float centerY = relativeLayout.getHeight() / 2.0f;
		final Rotate3dAnimation rotation = new Rotate3dAnimation(start, end,
				centerX, centerY, 310.0f, true);
		rotation.setDuration(700);
		rotation.setFillAfter(true);
		rotation.setInterpolator(new AccelerateInterpolator());
		rotation.setAnimationListener(new DisplayNextView());
		relativeLayout.startAnimation(rotation);
	}

	private final class DisplayNextView implements AnimationListener {

		@Override
		public void onAnimationStart(Animation animation) {

		}

		@Override
		public void onAnimationEnd(Animation animation) {
			relativeLayout.post(new SwapViews());
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}

	}

	private final class SwapViews implements Runnable {
		public void run() {
			final float centerX = relativeLayout.getWidth() / 2.0f;
			final float centerY = relativeLayout.getHeight() / 2.0f;
			Rotate3dAnimation rotation;
			imageLayout.setVisibility(View.GONE);
			rotation = new Rotate3dAnimation(0, 180, centerX, centerY, 310.0f,
					false);
			rotation.setDuration(700);
			rotation.setFillAfter(true);
			rotation.setInterpolator(new DecelerateInterpolator());
			relativeLayout.startAnimation(rotation);

			Intent intent = new Intent(StartActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}
		
	}


	@Override
	protected void doRouter(String routerIp, String routerPort,String routerMac) {
		
		Log.d("Mylog", "routerIp="+routerIp);
	}
	
	@Override
	protected void doDrive(String driveIp, String drivePort, String driveMac) {
		Log.d("Mylog", "driveIp="+driveIp);

		if (!StringUtil.isEmpty(driveIp)) {
			Log.d("Mylog", "asdfasdfsf");
			dismissProgressDialog();
//			applyRotation(0, 180);
		}
		
	}

	@Override
	protected void doError(String errorMessage) {
		
		
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
