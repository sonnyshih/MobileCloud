package com.sonnyshih.mobilecloud.activity.uploadfile;


import java.io.File;
import java.util.ArrayList;

import com.sonnyshih.mobilecloud.entity.ActionModeFileEntity;
import com.sonnyshih.mobilecloud.manage.WebDavManager;
import com.sonnyshih.mobilecloud.manage.WebDavManager.UploadHandler;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class UploadFileService extends Service implements UploadHandler{
	private final IBinder serviceBinder = new ServiceBinder();
	
	private ArrayList<ActionModeFileEntity> uploadFileArrayList;
	private boolean isNotCompleted = true;
	private boolean isStopUpload = true;
	private int progress = 0;
	private File currentUploadFile;
	private int currentNumber = 0;
	
	public ArrayList<ActionModeFileEntity> getUploadFileArrayList() {
		return uploadFileArrayList;
	}

	public boolean isNotCompleted() {
		return isNotCompleted;
	}

	public int getProgress() {
		return progress;
	}
	
	public File getCurrentUploadFile() {
		return currentUploadFile;
	}
	
	public int getCurrentNumber() {
		return currentNumber;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}

	
	class ServiceBinder extends Binder {
		
		public UploadFileService getService () {
			return UploadFileService.this;
		}
	}
	
	public void startUpload(final String uploadPath,
			final ArrayList<ActionModeFileEntity> uploadFileArrayList) {
		
		this.uploadFileArrayList = uploadFileArrayList;
		
		new Thread (new Runnable() {
			
			@Override
			public void run() {
				currentNumber = 0;
				isNotCompleted = true;
				isStopUpload = false;
				
				for (ActionModeFileEntity actionModeFileEntity : uploadFileArrayList) {
					currentNumber++;
					currentUploadFile = actionModeFileEntity.getFile();
					progress = 0;
					// stop uploading file;
					if (isStopUpload) {
						isNotCompleted = false;
						return;
					}
					
					boolean isExsitOnWebDav = actionModeFileEntity.isExsitOnWebDav();
					String fileName = actionModeFileEntity.getFile().getName();
					String fileLocalPath = actionModeFileEntity.getFile().getPath();
					
					WebDavManager.getInstance().uploadFile(
							UploadFileService.this, isExsitOnWebDav, fileName,
							uploadPath, fileLocalPath);

				}				
				
				isNotCompleted = false;
			}
		}).start();
	}
	
	public void stopUpload(){
		isStopUpload = true;
	}
	
	@Override
	public void getProgress(int progress) {
		this.progress = progress;
	}

	@Override
	public void getMessage(int statusCode, String statusText) {
		
	}

}
