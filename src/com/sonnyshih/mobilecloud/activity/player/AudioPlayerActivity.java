package com.sonnyshih.mobilecloud.activity.player;

import java.util.ArrayList;
import java.util.Date;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sonnyshih.mobilecloud.R;
import com.sonnyshih.mobilecloud.base.BaseFragmentActivity;
import com.sonnyshih.mobilecloud.entity.WebDavItemEntity;
import com.sonnyshih.mobilecloud.fragment.home.CloudFragment;

public class AudioPlayerActivity extends BaseFragmentActivity implements
		ServiceConnection, OnClickListener {

	private ArrayList<WebDavItemEntity> webDavItemEntities = null;
	private int filePosition = 0;
	
	private boolean isThreadEnable;
	private ImageView previousImageView;
	private ImageView pauseImageView;
	private ImageView playImageView;
	private ImageView nextImageView;
	private SeekBar seekBar;
	private TextView collapsedTextView;
	private TextView durationTextView;
	
	private AudioPlayerService audioPlayerService;
	private String audioPlayerClassName = AudioPlayerService.class.getName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audio_player_activity);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		filePosition = bundle.getInt(CloudFragment.BUNDLE_INT_FILE_POSITION);
		webDavItemEntities = (ArrayList<WebDavItemEntity>) bundle
				.getSerializable(CloudFragment.BUNDLE_ARRAYLIST_AUDIO_WEBDAV_ITEM_ENTITIES);
		
		init();
		startAudioPlayerService();
	}

	private void init(){
		
		seekBar = (SeekBar) findViewById(R.id.audioPlayer_seekBar);
		seekBar.setProgress(0);
		seekBar.setSecondaryProgress(0);
		
		collapsedTextView = (TextView) findViewById(R.id.audioPlayer_collapsedTextView);
		durationTextView = (TextView) findViewById(R.id.audioPlayer_durationTextView);
		
		previousImageView = (ImageView) findViewById(R.id.audioPlayer_previousImageView);
		previousImageView.setOnClickListener(this);
		
		pauseImageView = (ImageView) findViewById(R.id.audioPlayer_pauseImageView);
		pauseImageView.setOnClickListener(this);

		playImageView = (ImageView) findViewById(R.id.audioPlayer_playImageView);
		playImageView.setOnClickListener(this);

		nextImageView = (ImageView) findViewById(R.id.audioPlayer_nextImageView);
		nextImageView.setOnClickListener(this);

	}

	private void startAudioPlayerService(){
		Intent intent = new Intent(this, AudioPlayerService.class);
		
		if (!isServiceRunning(audioPlayerClassName)) {
			startService(intent);
		}
		
		bindService(intent, this, Context.BIND_AUTO_CREATE);
		
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		isThreadEnable = false;
		unbindService(this);
		finish();
	}

	private Thread uiThread =  new Thread(new Runnable() {
		
		@Override
		public void run() {
			while (isThreadEnable) {
				
				try {
					Thread.sleep(100);
					Message message = new Message();
					message.what = 1;
					uiHandler.sendMessage(message);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}
	});
	
	
	private Handler uiHandler = new Handler(){

		@Override
		public void handleMessage(Message message) {
			super.handleMessage(message);
			
			switch (message.what) {
			case 1:
				
				MediaPlayer mediaPlayer = audioPlayerService.getMediaPlayer();
				if (mediaPlayer != null) {
					final int progressPosition = mediaPlayer
							.getCurrentPosition();
					seekBar.setProgress(progressPosition);

					int percent = audioPlayerService.getPercent();
					seekBar.setSecondaryProgress(seekBar.getMax() / 100
							* percent);

					collapsedTextView.setText(String.format("%1$tM:%1$tS",
							new Date(progressPosition)));

					durationTextView.setText(String.format("%1$tM:%1$tS",
							new Date(mediaPlayer.getDuration())));
				}

				break;

			default:
				break;
			}
		}
		
	};
	
	public boolean isServiceRunning(String className) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (className.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		audioPlayerService = ((AudioPlayerService.ServiceBinder) service)
				.getService();

		
		// roll back the data.
		if (webDavItemEntities == null) {
			filePosition = audioPlayerService.getFilePosition();
			webDavItemEntities = audioPlayerService.getWebDavItemEntities();

		} else {
			String playUrl = webDavItemEntities.get(filePosition).getPlayUrl();
			audioPlayerService.setFilePosition(filePosition);
			audioPlayerService.setWebDavItemEntities(webDavItemEntities);
			audioPlayerService.startMediaPlayer(playUrl);
			
		}

		MediaPlayer mediaPlayer = audioPlayerService.getMediaPlayer();
		
		isThreadEnable = true;
		seekBar.setMax(mediaPlayer.getDuration());
		uiThread.start();

	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		audioPlayerService = null;
	}

	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.audioPlayer_previousImageView:
			audioPlayerService.previousAudio();
			break;

		case R.id.audioPlayer_pauseImageView:
			audioPlayerService.pauseAudio();
			break;

		case R.id.audioPlayer_playImageView:
			audioPlayerService.playAudio();
			break;

		case R.id.audioPlayer_nextImageView:
			audioPlayerService.nextAudio();
			break;

		default:
			break;
		}
	}

	
}
