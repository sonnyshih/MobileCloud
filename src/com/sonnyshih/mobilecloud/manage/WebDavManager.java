package com.sonnyshih.mobilecloud.manage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;

import android.R.integer;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.sonnyshih.mobilecloud.upload.UploadProgressListener;
import com.sonnyshih.mobilecloud.upload.UploadRequestEntity;
import com.sonnyshih.mobilecloud.util.StringUtil;

public class WebDavManager {

	private static WebDavManager instance;
	
	private static HttpClient Client;
	private String host;
	private String port;
	private String username;
	private String password;
	
	
	public WebDavManager(){
	}
	
	public static WebDavManager getInstance() {
		if (instance == null) {
			instance = new WebDavManager();
		}
		return instance;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
	public void settingWebdave(){
		HostConfiguration hostConfig = new HostConfiguration();
		hostConfig.setHost(host, Integer.parseInt(port));

		int maxHostConnections = 20;
		HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setMaxConnectionsPerHost(hostConfig, maxHostConnections);
		
		connectionManager.setParams(params);

		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);

		Client = new HttpClient(connectionManager);
		Client.getState().setCredentials(AuthScope.ANY, creds);
		Client.setHostConfiguration(hostConfig);
	
	}
	
	public MultiStatusResponse[] getFileList(String path){
		
		String fullPath;
		
		if (StringUtil.isEmpty(path)) {
			fullPath = "http://" + host + ":" + port + "/";
		} else {
			fullPath = "http://" + host + ":" + port + "/" + path + "/";
		}
		
		
		PropFindMethod propFindMethod;
		MultiStatusResponse[] Responses = null;
		
		try {
			propFindMethod = new PropFindMethod(fullPath, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
			Client.executeMethod(propFindMethod);
			MultiStatus multiStatus = propFindMethod.getResponseBodyAsMultiStatus();
			Responses = multiStatus.getResponses();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DavException e) {
			e.printStackTrace();
		}
		
		return Responses;

	}
	
	// create the new folder
	public void createNewFolder(String path){
		MkColMethod mkCol = new MkColMethod(path);
        try {
        	Client.executeMethod(mkCol);
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

	}
	
	
	public void uploadFile(final UploadHandler uploadHandler,
			boolean isFileExist, String fileName, String uploadPath,
			String fileLocalPath) {

		String path = "";
		if (isFileExist) {
			path = uploadPath + "/Copy_" + fileName;
		} else {
			path = uploadPath + "/" + fileName;
		}

		try {
			path = StringUtil.pathEncodeURL(path);
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
		
		
		String url;
		url = "http://" + host + ":" + port + path;
		

		File file = new File(fileLocalPath);
		long bytesToSend = file.length();
		
		FileRequestEntity entity = new FileRequestEntity(file,
				getContentType(file));
		
		UploadRequestEntity requestEntity = new UploadRequestEntity(entity,
				new UploadProgressListener(new Observer() {

					@Override
					public void update(Observable observable, Object data) {
//						Log.d("Mylog", "data=" + data.toString() + " int = "
//								+ Float.parseFloat(data.toString()));
						//						progress = (int) Float.parseFloat(data.toString());
						
						uploadHandler.getProgress((int) Float.parseFloat(data.toString()));
						
					}
				}, bytesToSend));
		
		PutMethod putMethod = new PutMethod(url);
		putMethod.setRequestEntity(requestEntity);

		try {
			Client.executeMethod(putMethod);
//			Log.d("Mylog", putMethod.getStatusCode() + " " + putMethod.getStatusText());
//			Log.d("Mylog", "ResponseBody as String: "+putMethod.getResponseBodyAsString());
//		
//			int response = ((HttpMethod) putMethod).getStatusCode();
//			Log.d("Mylog", "succeed: "+putMethod.succeeded());
//			Log.d("Mylog", "response code ="+ response);

			uploadHandler.getMessage(putMethod.getStatusCode(), putMethod.getStatusText());
			
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
//		try {
//			FileRequestEntity entity = new FileRequestEntity(file,
//					getContentType(file));
//			UploadRequestEntity requestEntity;
//			long bytesToSend = file.length();
//			// progressFlag = false;
//			// if(txtFileName != null)
//			// handle.post(new Runnable() {
//			// public void run() {
//			// txtFileName.setText("Uploading "+fileName);
//			// }
//			// });
//			requestEntity = new UploadRequestEntity(entity,
//					new UploadProgressListener(new Observer() {
//						public void update(Observable observable, Object data) {
//							// System.out.println("data="+data.toString());
//							// System.out.println("progress="+progress);
//							// if(Float.parseFloat(data.toString()) >= 50.0)
//							// progressFlag = true;
//							// if(!progressFlag)
//							// progress = (int)
//							// Float.parseFloat(data.toString());
//							// else
//							// if(Float.parseFloat(data.toString()) < 50)
//							// progress = 50 + (int)
//							// Float.parseFloat(data.toString());
//							// handle.post(new Runnable() {
//							// public void run() {
//							// if(txtPercent != null)
//							// txtPercent.setText(progress+"%");
//							// if(progressBar != null)
//							// progressBar.setProgress(progress);
//							// }
//							// });
//
//						}
//					}, bytesToSend));
//			putMethod.setRequestEntity(requestEntity);
//
//			Client.executeMethod(putMethod);
//			
//		} catch (FileNotFoundException e1) {
//			
//		} catch (HttpException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

	}
	
	public static String getContentType(File f) {
		Uri selectedUri = Uri.fromFile(f);
		String fileExtension = MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString());
	    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
	    return mimeType;
	}
	
	public interface UploadHandler {
		public void getProgress(int progress);
		public void getMessage(int statusCode, String statusText);
	}
	
}
