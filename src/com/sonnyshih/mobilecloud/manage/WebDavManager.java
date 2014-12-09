package com.sonnyshih.mobilecloud.manage;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.FileRequestEntity;
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

import com.sonnyshih.mobilecloud.upload.UploadRequestEntity;
import com.sonnyshih.mobilecloud.upload.UploadRequestEntity.ProgressListener;
import com.sonnyshih.mobilecloud.util.StringUtil;

public class WebDavManager {

	private static WebDavManager instance;
	
	private static HttpClient Client;
	private PutMethod putMethod;
	private CopyMethod copyMethod;
	private MoveMethod moveMethod;
	private DeleteMethod deleteMethod;
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

	public PutMethod getPutMethod() {
		return putMethod;
	}

	public void setPutMethod(PutMethod putMethod) {
		this.putMethod = putMethod;
	}

	public CopyMethod getCopyMethod() {
		return copyMethod;
	}

	public void setCopyMethod(CopyMethod copyMethod) {
		this.copyMethod = copyMethod;
	}

	public MoveMethod getMoveMethod() {
		return moveMethod;
	}

	public void setMoveMethod(MoveMethod moveMethod) {
		this.moveMethod = moveMethod;
	}

	public DeleteMethod getDeleteMethod() {
		return deleteMethod;
	}

	public void setDeleteMethod(DeleteMethod deleteMethod) {
		this.deleteMethod = deleteMethod;
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
		
		putMethod = new PutMethod(url);

		UploadRequestEntity requestEntity = new UploadRequestEntity(
				entity, new ProgressListener() {

					@Override
					public void transferred(long num) {
						uploadHandler.getProgress((int) num);
					}
				});
		
		putMethod.setRequestEntity(requestEntity);

		try {
			Client.executeMethod(putMethod);
			uploadHandler.getMessage(putMethod.getStatusCode(), putMethod.getStatusText());
			
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		putMethod = null;
	}
	
	public String getContentType(File f) {
		Uri selectedUri = Uri.fromFile(f);
		String fileExtension = MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString());
	    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
	    return mimeType;
	}
	

	// copy webdav file
	public void copyWebDavItem(String originalUrl , String destintionPath, String destinationName){
		
		String path = destintionPath + "/" + destinationName;
		path = StringUtil.pathEncodeURL(path);
		
		String destintionUrl;
		destintionUrl = "http://" + host + ":" + port + path;
		
		copyMethod = new CopyMethod(originalUrl, destintionUrl, false);
		
		try {
			Client.executeMethod(copyMethod);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		copyMethod = null;
	}
	
	
	// move webdav file
	public void moveWebDavItem(String originalUrl , String destintionPath, String destinationName){
		
		String path = destintionPath + "/" + destinationName;
		path = StringUtil.pathEncodeURL(path);
		
		String destintionUrl;
		destintionUrl = "http://" + host + ":" + port + path;

		moveMethod = new MoveMethod(originalUrl, destintionUrl, true);
		
		try {
			Client.executeMethod(moveMethod);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		moveMethod = null;
	}
	
	// delete webdav file.
	public void deleteWebDavItem(String path){
        deleteMethod = new DeleteMethod(path);
        
        try {
        	Client.executeMethod(deleteMethod);
        	
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        deleteMethod = null;

	}
	
	
	public interface UploadHandler {
		public void getProgress(int progress);
		public void getMessage(int statusCode, String statusText);
	}
	
}
