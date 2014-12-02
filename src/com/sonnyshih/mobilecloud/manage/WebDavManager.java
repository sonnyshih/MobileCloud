package com.sonnyshih.mobilecloud.manage;

import java.io.IOException;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;

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
	
}
