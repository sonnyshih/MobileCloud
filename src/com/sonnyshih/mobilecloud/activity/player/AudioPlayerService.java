package com.sonnyshih.mobilecloud.activity.player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.sonnyshih.mobilecloud.R;
import com.sonnyshih.mobilecloud.entity.WebDavItemEntity;
import com.sonnyshih.mobilecloud.util.StringUtil;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Binder;
import android.os.IBinder;

public class AudioPlayerService extends Service implements
		OnCompletionListener, OnErrorListener, OnBufferingUpdateListener {
	private final IBinder serviceBinder = new ServiceBinder();
	private MediaPlayer mediaPlayer;
	private ArrayList<WebDavItemEntity> webDavItemEntities;
	private int filePosition;
	private int percent = 0;
	private boolean isGetMetaData = false;
	private String albumName = "";
	private String name = "";
	private Bitmap albumArtBitmap = null;
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		releaseMediaPlayer();		
	}	
	
	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}
	
	
	public void startMediaPlayer(String playUrl){
		getMedaData(playUrl);
		
		try {
			releaseMediaPlayer();
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setDataSource(playUrl);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.prepare();
			mediaPlayer.start();
			mediaPlayer.setOnCompletionListener(this);
			mediaPlayer.setOnErrorListener(this);
			mediaPlayer.setOnBufferingUpdateListener(this);
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}	

	private void releaseMediaPlayer(){
		if (mediaPlayer == null) {
			return;
		}
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}
		mediaPlayer.release();
		mediaPlayer = null;
	}
	
	
	public ArrayList<WebDavItemEntity> getWebDavItemEntities() {
		return webDavItemEntities;
	}

	public void setWebDavItemEntities(ArrayList<WebDavItemEntity> webDavItemEntities) {
		this.webDavItemEntities = webDavItemEntities;
	}

	public int getFilePosition() {
		return filePosition;
	}

	public void setFilePosition(int filePosition) {
		this.filePosition = filePosition;
	}
	
	public MediaPlayer getMediaPlayer() {
		return mediaPlayer;
	}

	public void previousAudio(){
		
		int total = webDavItemEntities.size();
		filePosition--;
		if (filePosition < 0) {
			filePosition = total - 1;
		}
		
		String playUrl = webDavItemEntities.get(filePosition).getPlayUrl();
		startMediaPlayer(playUrl);
	}
	
	public void nextAudio(){
		int total = webDavItemEntities.size();
		filePosition++;
		if (filePosition >= total) {
			filePosition = 0;
		}

		String playUrl = webDavItemEntities.get(filePosition).getPlayUrl();
		startMediaPlayer(playUrl);
	}

	public void pauseAudio(){
		if (mediaPlayer != null) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}
		}
	}
	
	public void playAudio(){
		if (mediaPlayer != null) {
			mediaPlayer.start();
		}
	}
	
	public int getPercent() {
		return percent;
	}

	public boolean isGetMetaData() {
		return isGetMetaData;
	}

	public Bitmap getAlbumArtBitmap() {
		return albumArtBitmap;
	}

	public String getAlbumName() {
		return albumName;
	}

	public String getName() {
		return name;
	}

	private void getMedaData(final String url) {
		isGetMetaData = false;
		
		new Thread (new Runnable() {
			
			public void run() {
				MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
				HashMap<String, String> headersMap = new HashMap<String, String>();
				mediaMetadataRetriever.setDataSource(url, headersMap);

				albumName = mediaMetadataRetriever
						.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
				
				if (StringUtil.isEmpty(albumName)) {
					albumName = "N.A";
				}

				name = mediaMetadataRetriever
						.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
				
				if (StringUtil.isEmpty(name)) {
					name = webDavItemEntities.get(filePosition).getName();
				}
				
				
				byte[] embeddedPicture = mediaMetadataRetriever.getEmbeddedPicture();
				albumArtBitmap = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_album);
				
				if (embeddedPicture != null) {
					albumArtBitmap = BitmapFactory.decodeByteArray(
							embeddedPicture, 0, embeddedPicture.length);
				}
				
	        	 isGetMetaData = true;
	        	 

			}
		}).start();
	}//end getAlbumName
	
	class ServiceBinder extends Binder {
		
		public AudioPlayerService getService () {
			return AudioPlayerService.this;
		}
	}


	@Override
	public void onCompletion(MediaPlayer mp) {
		
		int total = webDavItemEntities.size();
		filePosition++;
		if (filePosition >= total) {
			filePosition = 0;
		}
		
		String playUrl = webDavItemEntities.get(filePosition).getPlayUrl();
		startMediaPlayer(playUrl);
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
//		switch (what) {
//		case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
//			Toast.makeText(this,"The audio is stopped suddenly. Please try again", Toast.LENGTH_SHORT).show();
//			break;
//		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
//			Toast.makeText(this, "The audio is stopped suddenly. Please try again",
//					Toast.LENGTH_SHORT).show();
//			break;
//		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
//			Toast.makeText(this, "The audio is stopped suddenly. Please try again",
//					Toast.LENGTH_SHORT).show();
//			break;
//		}
		return false;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		this.percent = percent;
	}
}
