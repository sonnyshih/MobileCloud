package com.sonnyshih.mobilecloud.activity.uploadfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.sonnyshih.mobilecloud.entity.ActionModeFileEntity;
import com.sonnyshih.mobilecloud.entity.WebDavItemEntity;
import com.sonnyshih.mobilecloud.manage.ApplicationManager;
import com.sonnyshih.mobilecloud.manage.WebDavManager;
import com.sonnyshih.mobilecloud.manage.WebDavManager.DownloadHandler;
import com.sonnyshih.mobilecloud.manage.WebDavManager.UploadHandler;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class WebDaveService extends Service implements UploadHandler,
		DownloadHandler {
	
	private final IBinder serviceBinder = new ServiceBinder();
	
	private ArrayList<ActionModeFileEntity> uploadFileArrayList;
	private boolean isStopUpload = true;
	private int uploadFileProgress = 0;
	private File currentUploadFile;
	private int currentNumber = 0;
	
	private ArrayList<WebDavItemEntity> downloadWebDavItemEntities;
	private boolean isStopDownload = true;
	private long downloadFileProgress = 0;
	private String currentDownloadFileName;
	private long downloadFileLength;

	private OutputStream outputStream;
	
	public ArrayList<ActionModeFileEntity> getUploadFileArrayList() {
		return uploadFileArrayList;
	}

	public boolean isStopUpload() {
		return isStopUpload;
	}

	public int getUploadFileProgress() {
		return uploadFileProgress;
	}
	
	public File getCurrentUploadFile() {
		return currentUploadFile;
	}
	
	public int getCurrentNumber() {
		return currentNumber;
	}

	
	public ArrayList<WebDavItemEntity> getDownloadWebDavItemEntities() {
		return downloadWebDavItemEntities;
	}

	public String getCurrentDownloadFileName() {
		return currentDownloadFileName;
	}

	public long getDownloadFileProgress() {
		return downloadFileProgress;
	}

	public long getDownloadFileLength() {
		return downloadFileLength;
	}

	public boolean isStopDownload() {
		return isStopDownload;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}

	
	public class ServiceBinder extends Binder {
		
		public WebDaveService getService () {
			return WebDaveService.this;
		}
	}
	
	public void startUpload(final String uploadPath,
			final ArrayList<ActionModeFileEntity> uploadFileArrayList) {
		
		this.uploadFileArrayList = uploadFileArrayList;
		
		new Thread (new Runnable() {
			
			@Override
			public void run() {
				currentNumber = 0;
				isStopUpload = false;
				
				for (ActionModeFileEntity actionModeFileEntity : uploadFileArrayList) {
					currentNumber++;
					currentUploadFile = actionModeFileEntity.getFile();
					uploadFileProgress = 0;
					
					// stop uploading file;
					if (isStopUpload) {
						return;
					}
					
					boolean isExsitOnWebDav = actionModeFileEntity.isExsitOnWebDav();
					String fileName = actionModeFileEntity.getFile().getName();
					String fileLocalPath = actionModeFileEntity.getFile().getPath();
					
					WebDavManager.getInstance().setAbort(false);
					WebDavManager.getInstance().uploadFile(
							WebDaveService.this, isExsitOnWebDav, fileName,
							uploadPath, fileLocalPath);

				}				
				
				isStopUpload = true;
			}
		}).start();
	}
	
	public void stopUpload(){
		isStopUpload = true;
		WebDavManager.getInstance().setAbort(true);
	}
	
	public void startDownload(final ArrayList<WebDavItemEntity> downloadWebDavItemEntities){
		
		this.downloadWebDavItemEntities = downloadWebDavItemEntities;
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				currentNumber = 0;
				isStopDownload = false;
				
				for (WebDavItemEntity webDavItemEntity : downloadWebDavItemEntities) {
					currentNumber++;
					currentDownloadFileName = webDavItemEntity.getName();
					
					// Stop Download file.
					if (isStopDownload) {
						return;
					}
					
					WebDavManager.getInstance().setAbort(false);
					WebDavManager.getInstance().downloadFile(
							WebDaveService.this, webDavItemEntity.getName(),
							webDavItemEntity.getPlayUrl());
				}
				
				isStopDownload = true;
				
			}
		}).start();
		
	}
	
	public void stopDownload(){
		isStopDownload = true;
		WebDavManager.getInstance().setAbort(true);
	}
	
	@Override
	public void getUploadProgress(int uploadFileProgress) {
		this.uploadFileProgress = uploadFileProgress;
	}

	@Override
	public void getUploadMessage(int statusCode, String statusText) {
		
	}

	@Override
	public void getDownloadInputStream(String fileName, InputStream downloadFileInputStream,
			long downloadFileLength) {

		this.downloadFileLength = downloadFileLength;
		downloadFileProgress = 0;
		
		String localPath = ApplicationManager.getInstance()
				.getDownloadFolderPath() + "/" + fileName;
		
		// Create a new file stream
		try {
			outputStream = new FileOutputStream(localPath);
			int bufferSize = -1;	// calculate the size of reading data.
	        byte[] buffer = new byte[1024];		// Create the 1024 bytes Buffer
	        
	        while ((bufferSize = downloadFileInputStream.read(buffer)) != -1) {
	        	
	        	if (WebDavManager.getInstance().isAbort()) {
	           		WebDavManager.getInstance().getGetMethod().abort();
	        		break;
				}
	        	
	        	outputStream.write(buffer, 0, bufferSize);
	        	downloadFileProgress += bufferSize;
	        }

	        downloadFileInputStream.close();
	        outputStream.flush();
	        outputStream.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();

			// When getMethod is aborted, the BufferedInputStream is closed
			// The exist file will be deleted.
	        try {
				outputStream.flush();
		        outputStream.close();
		        
	    		// remove the abort file.
	    		File file = new File(localPath);
	    		if (file.exists()) {
	        		file.delete();
				}
	    		
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}

	}

	@Override
	public void getDownloadMessage(int statusCode, String statusText) {
		
	}

}
