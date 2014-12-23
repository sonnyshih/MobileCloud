package com.sonnyshih.mobilecloud.manage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.CopyMethod;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.sonnyshih.mobilecloud.entity.UploadRequestEntity;
import com.sonnyshih.mobilecloud.entity.UploadRequestEntity.ProgressListener;
import com.sonnyshih.mobilecloud.util.StringUtil;

public class WebDavManager {

	private static WebDavManager instance;
	
	private HttpClient httpClient;
	private String host;
	private String port;
	private String username;
	private String password;
	private boolean isAbort = false;
	private GetMethod getMethod;
	
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
	
	public boolean isAbort() {
		return isAbort;
	}

	public void setAbort(boolean isAbort) {
		this.isAbort = isAbort;
	}

	public GetMethod getGetMethod() {
		return getMethod;
	}

	public void initWebdave(){
		HostConfiguration hostConfig = new HostConfiguration();
		hostConfig.setHost(host, Integer.parseInt(port));

		int maxHostConnections = 20;
		HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setMaxConnectionsPerHost(hostConfig, maxHostConnections);
		
		connectionManager.setParams(params);

		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);

		httpClient = new HttpClient(connectionManager);
		httpClient.getState().setCredentials(AuthScope.ANY, creds);
		httpClient.setHostConfiguration(hostConfig);
	
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
			httpClient.executeMethod(propFindMethod);
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
        	httpClient.executeMethod(mkCol);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		

	}
	
	// upload file
	public void uploadFile(final UploadHandler uploadHandler,
			boolean isFileExist, String fileName, String uploadPath,
			String fileLocalPath) {

		String path = "";
		if (isFileExist) {
			path = uploadPath + "/Copy_" + fileName;
		} else {
			path = uploadPath + "/" + fileName;
		}

		path = StringUtil.pathEncodeURL(path);
		
		String url;
		url = "http://" + host + ":" + port + path;
		
		File file = new File(fileLocalPath);
		
		FileRequestEntity entity = new FileRequestEntity(file,
				getContentType(file));
		
		final PutMethod putMethod = new PutMethod(url);

		UploadRequestEntity requestEntity = new UploadRequestEntity(
				entity, new ProgressListener() {

					@Override
					public void transferred(long num) {
						uploadHandler.getUploadProgress((int) num);
						if (isAbort) {
							putMethod.abort();
						}
					}
				});
		
		putMethod.setRequestEntity(requestEntity);

		try {
			httpClient.executeMethod(putMethod);
			uploadHandler.getUploadMessage(putMethod.getStatusCode(),
					putMethod.getStatusText());
			
//			Log.d("Mylog", putMethod.getStatusCode() + " : " + putMethod.getStatusText());
//			Log.d("Mylog", "ResponseBody as String: "+putMethod.getResponseBodyAsString());
//		
//			int response = ((HttpMethod) putMethod).getStatusCode();
//			Log.d("Mylog", "succeed: "+putMethod.succeeded());
//			Log.d("Mylog", "response code ="+ response);
			
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			putMethod.releaseConnection();
		}

	}
	
	public String getContentType(File f) {
		Uri selectedUri = Uri.fromFile(f);
		String fileExtension = MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString());
	    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
	    return mimeType;
	}
	
	public void downloadFile(DownloadHandler downloadHandler, String fileName, String path) {

		getMethod = new GetMethod(path);
		
        try {
        	httpClient.executeMethod(getMethod);
        	
			downloadHandler.getDownloadInputStream(fileName,
					getMethod.getResponseBodyAsStream(),
					getMethod.getResponseContentLength());

			downloadHandler.getDownloadMessage(getMethod.getStatusCode(),
					getMethod.getStatusText());        	

		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			getMethod.releaseConnection();
		}

	}
	
	// copy webdav file
	public void copyWebDavItem(String originalUrl , String destintionPath, String destinationName){
		
		String path = destintionPath + "/" + destinationName;
		path = StringUtil.pathEncodeURL(path);
		
		String destintionUrl;
		destintionUrl = "http://" + host + ":" + port + path;
		
		CopyMethod copyMethod = new CopyMethod(originalUrl, destintionUrl, false);
		
		try {
			httpClient.executeMethod(copyMethod);
			if (isAbort) {
				copyMethod.abort();
			}
			
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			copyMethod.releaseConnection();
		}
		
		copyMethod = null;
	}
	
	
	// move webdav file
	public void moveWebDavItem(String originalUrl , String destintionPath, String destinationName){
		
		String path = destintionPath + "/" + destinationName;
		path = StringUtil.pathEncodeURL(path);
		
		String destintionUrl;
		destintionUrl = "http://" + host + ":" + port + path;

		MoveMethod moveMethod = new MoveMethod(originalUrl, destintionUrl, true);
		
		try {
			httpClient.executeMethod(moveMethod);
			if (isAbort) {
				moveMethod.abort();
			}

		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			moveMethod.releaseConnection();
		}
		
		moveMethod = null;
	}
	
	// delete webdav file.
	public void deleteWebDavItem(String path){
        DeleteMethod deleteMethod = new DeleteMethod(path);
        
        try {
        	httpClient.executeMethod(deleteMethod);
			if (isAbort) {
				deleteMethod.abort();
			}

		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			deleteMethod.releaseConnection();
		}
        
        deleteMethod = null;

	}
	
	
	public interface UploadHandler {
		public void getUploadProgress(int uploadFileProgress);
		public void getUploadMessage(int statusCode, String statusText);
	}
	
	public interface DownloadHandler {
		public void getDownloadInputStream(String fileName, InputStream downloadFileInputStream, long downloadFileLength);
		public void getDownloadMessage(int statusCode, String statusText);
	}
	
}
