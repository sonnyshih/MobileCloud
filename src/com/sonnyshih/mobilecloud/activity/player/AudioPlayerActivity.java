package com.sonnyshih.mobilecloud.activity.player;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.sonnyshih.mobilecloud.R;
import com.sonnyshih.mobilecloud.base.BaseFragmentActivity;
import com.sonnyshih.mobilecloud.entity.WebDavItemEntity;
import com.sonnyshih.mobilecloud.fragment.home.CloudFragment;

public class AudioPlayerActivity extends BaseFragmentActivity implements OnClickListener{

	private ArrayList<WebDavItemEntity> webDavItemEntities;
	private int filePosition;
	
	private boolean isSeekBarThreadRunning;
	private MediaPlayer mediaPlayer = new MediaPlayer();
	
	private ImageView previousImageView;
	private ImageView pauseImageView;
	private ImageView playImageView;
	private ImageView nextImageView;
	private SeekBar seekBar;
	
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
		startMediaPlayer();
	}

	private void init(){
		
		seekBar = (SeekBar) findViewById(R.id.audioPlayer_seekBar);
		seekBar.setProgress(0);
		seekBar.setSecondaryProgress(0);
		
		previousImageView = (ImageView) findViewById(R.id.audioPlayer_previousImageView);
		previousImageView.setOnClickListener(this);
		
		pauseImageView = (ImageView) findViewById(R.id.audioPlayer_pauseImageView);
		pauseImageView.setOnClickListener(this);

		playImageView = (ImageView) findViewById(R.id.audioPlayer_playImageView);
		playImageView.setOnClickListener(this);

		nextImageView = (ImageView) findViewById(R.id.audioPlayer_nextImageView);
		nextImageView.setOnClickListener(this);

	}
	
	private void startMediaPlayer(){
		try {
			mediaPlayer.setDataSource(webDavItemEntities.get(filePosition).getPlayUrl());
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		isSeekBarThreadRunning = true;
		seekBar.setMax(mediaPlayer.getDuration());
		seekBarThread.start();
		
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		isSeekBarThreadRunning = false;
		finish();
	}

	private Thread seekBarThread =  new Thread(new Runnable() {
		
		@Override
		public void run() {
			while (isSeekBarThreadRunning) {
				int seedBarPosition = mediaPlayer.getCurrentPosition();
				seekBar.setProgress(seedBarPosition);
			}
		}
	});
	
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.audioPlayer_previousImageView:
			
			break;

		case R.id.audioPlayer_pauseImageView:
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();	
			}
			
			break;

		case R.id.audioPlayer_playImageView:
			mediaPlayer.start();
			break;

		case R.id.audioPlayer_nextImageView:
			
			break;

		default:
			break;
		}
	}

	
}
