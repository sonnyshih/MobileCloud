package com.sonnyshih.mobilecloud.fragment.home;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;

import com.sonnyshih.mobilecloud.R;
import com.sonnyshih.mobilecloud.activity.home.MainActivity;
import com.sonnyshih.mobilecloud.base.BaseFragment;
import com.sonnyshih.mobilecloud.entity.ItemType;
import com.sonnyshih.mobilecloud.entity.WebDavItemEntity;
import com.sonnyshih.mobilecloud.manage.ApplicationManager;
import com.sonnyshih.mobilecloud.manage.WebDavManager;
import com.sonnyshih.mobilecloud.ui.adapter.CloudFileListAdapter;
import com.sonnyshih.mobilecloud.util.StringUtil;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

public class CloudFragment extends BaseFragment implements OnItemClickListener{
	
	private String parentPath = "";
	private String currentPath = "";
	
	private WebDavManager webDavManager;
	private ArrayList<WebDavItemEntity> webDavItemEntities = new ArrayList<WebDavItemEntity>();
	private ListView fileListView;
	private CloudFileListAdapter cloudFileListAdapter;
	private ProgressBar progressBar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.cloud_fragement, container, false);
		
		progressBar = (ProgressBar) view.findViewById(R.id.cloud_progressBar);
		progressBar.setVisibility(View.VISIBLE);
		
		fileListView = (ListView) view.findViewById(R.id.cloud_listView);
		fileListView.setVisibility(View.GONE);
		cloudFileListAdapter = new CloudFileListAdapter(getActivity(), webDavItemEntities);
		fileListView.setAdapter(cloudFileListAdapter);
		fileListView.setOnItemClickListener(this);
		
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					if (MainActivity.isGetDriveIp) {
						initWebDav();
						showFileList(currentPath);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

	}
	
	private void initWebDav(){
		String host = ApplicationManager.getInstance().getDriveIp();
		String port = ApplicationManager.getInstance().getDrivePort();
		String username = ApplicationManager.getInstance().getWebDavUsername();
		String password = ApplicationManager.getInstance().getWebDavPassword();
		
		webDavManager = WebDavManager.getInstance();
		webDavManager.setHost(host);
		webDavManager.setPort(port);
		webDavManager.setUsername(username);
		webDavManager.setPassword(password);
		webDavManager.settingWebdave();

	}
	
	private void showFileList(final String path){
		
		progressBar.setVisibility(View.VISIBLE);
		fileListView.setVisibility(View.GONE);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				MultiStatusResponse[] multiStatusResponses = webDavManager.getFileList(path); 
				webDavItemEntities.clear();
				
				for (int i = 0; i < multiStatusResponses.length; i++) {
					
					if (i > 0) {	// ignore the first item.
						WebDavItemEntity webDavItemEntity = ganerateDavItemEntity(multiStatusResponses[i]);
						webDavItemEntities.add(webDavItemEntity);
						
					} else {
						
						if (!StringUtil.isEmpty(currentPath)) {
							WebDavItemEntity webDavItemEntity = new WebDavItemEntity(); 
							webDavItemEntity.setType(ItemType.Other);
							webDavItemEntity.setName("Up...");
							webDavItemEntities.add(webDavItemEntity);
						}
						
					}
				}
				
			    getActivity().runOnUiThread(new Runnable() {
			        @Override
			        public void run() {
			        	cloudFileListAdapter.notifyDataSetChanged();
			    		progressBar.setVisibility(View.GONE);
			    		fileListView.setVisibility(View.VISIBLE);

			        }
			    });
				
			}
		}).start();
		
	}

	private WebDavItemEntity ganerateDavItemEntity(MultiStatusResponse multiStatusResponse){
		
		WebDavItemEntity webDavItemEntity = new WebDavItemEntity();
		int status = multiStatusResponse.getStatus()[0].getStatusCode();
		DavPropertySet davPropertySet = multiStatusResponse.getProperties(status);
//		Log.d("Mylog","###################################");

		// Get content Type
		DavProperty<?> davProperty = davPropertySet.get(DavPropertyName.GETCONTENTTYPE);
		String contentType;
		if (davProperty != null) {
			contentType = (String) davProperty.getValue();
		} else {
			contentType = "None";
		}
//		Log.d("Mylog","content Type = " + contentType);
		
		// Get type
		if (contentType.equals("httpd/unix-directory")){
			webDavItemEntity.setType(ItemType.Folder);
		}else{
			webDavItemEntity.setType(ItemType.File);
		}//End if
		
//		Log.d("Mylog","Type = " + type);

		
		// Get Create Date
		davProperty = davPropertySet.get(DavPropertyName.PROPERTY_CREATIONDATE);
		String createDate = davProperty.getValue().toString();
		webDavItemEntity.setCreateDate(createDate);
//		Log.d("Mylog", "Create Date = " + createDate);
		
		// Get Last Modify Date
		davProperty = davPropertySet.get(DavPropertyName.GETLASTMODIFIED);
		String modifyDate = davProperty.getValue().toString();
		webDavItemEntity.setModifyDate(modifyDate);
//		Log.d("Mylog","Last Modify Date = " + modifyDate);

		// Get File Size
		davProperty = davPropertySet.get(DavPropertyName.GETCONTENTLENGTH);
		String size = davProperty.getValue().toString();
		webDavItemEntity.setSize(size);
//		Log.d("Mylog", "File Size = " + size);
		
		// Get file url 
		String fileUrl = multiStatusResponse.getHref();
		String fileUrlTemp = fileUrl;
//		Log.d("Mylog", "file Url = " + fileUrl);
		
		try {
			fileUrlTemp = java.net.URLDecoder.decode(fileUrlTemp , "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}//End try
//		Log.d("Mylog", "file Url temp = " + fileUrlTemp);

		
		String name ="";				
		davProperty = davPropertySet.get(DavPropertyName.DISPLAYNAME);
		if (davProperty != null){
			name = (String) davProperty.getName().toString();
		} else {
			String[] tmp = fileUrlTemp.split("/");
			if (tmp.length > 0) {
				name = tmp[tmp.length - 1];
			}//End if
		}//End if

		webDavItemEntity.setName(name);
//		Log.d("Mylog", "File Name = " + name);
 
		// Get Play URL
		String playUrl;		// URL with the Username and Password for Player
		String Username = ApplicationManager.getInstance().getWebDavUsername();
		String Password = ApplicationManager.getInstance().getWebDavPassword();
		String HostName = ApplicationManager.getInstance().getDriveIp();
		String HostPort = ApplicationManager.getInstance().getDrivePort();
		//Video_URL_Tmp = "http://admin:admin@192.168.1.1:8081/SD/Aximcom-sonny.mp4";
		playUrl = "http://" + Username + ":" + Password + "@" + HostName + ":"
				+ HostPort + "/" + currentPath + "/" + name;
		webDavItemEntity.setPlayUrl(playUrl);
//		Log.d("Mylog", "play url = " + playUrl);
		
		return webDavItemEntity;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		ItemType type = webDavItemEntities.get(position).getType();
		String itemName = webDavItemEntities.get(position).getName();
		
		switch (type) {
		
		case Folder:
			parentPath = currentPath; 
			currentPath += "/"+itemName;
			showFileList(currentPath);
			break;

		case File:
			
			break;

		case Other:
			showFileList(parentPath);
			currentPath = parentPath;
			
			// File's parent path
			if (!currentPath.equals("")) {
				int index = currentPath.lastIndexOf("/");
				parentPath = currentPath.substring(0, index);
			}
			break;

		default:
			break;
		}
		
		Log.d("Mylog", "parentPath="+parentPath);
		Log.d("Mylog", "currentPath="+currentPath);

		
//		if (itemName.equals("up")) {
//			showFileList(parentPath);
//			currentPath = parentPath;
//			
//			// File's parent path
//			if (!currentPath.equals("")) {
//				int index = currentPath.lastIndexOf("/");
//				parentPath = currentPath.substring(0, index);
//			}
//			
//			return;
//		}
		
//		if (type.equals("folder")) {
//			parentPath = currentPath; 
//			currentPath += "/"+itemName;
//
//			Log.d("Mylog", "parentPath="+parentPath);
//			Log.d("Mylog", "currentPath="+currentPath);
//			showFileList(currentPath);
//		}
		
		
	}
	
}
