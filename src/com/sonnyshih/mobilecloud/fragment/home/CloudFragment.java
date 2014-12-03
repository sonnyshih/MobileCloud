package com.sonnyshih.mobilecloud.fragment.home;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;

import com.sonnyshih.mobilecloud.R;
import com.sonnyshih.mobilecloud.activity.cloud.LocalFileListActivity;
import com.sonnyshih.mobilecloud.activity.home.MainActivity;
import com.sonnyshih.mobilecloud.base.BaseFragment;
import com.sonnyshih.mobilecloud.entity.FileType;
import com.sonnyshih.mobilecloud.entity.ItemType;
import com.sonnyshih.mobilecloud.entity.WebDavItemEntity;
import com.sonnyshih.mobilecloud.manage.ApplicationManager;
import com.sonnyshih.mobilecloud.manage.WebDavManager;
import com.sonnyshih.mobilecloud.ui.adapter.CloudFileListAdapter;
import com.sonnyshih.mobilecloud.util.FileUtil;
import com.sonnyshih.mobilecloud.util.StringUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CloudFragment extends BaseFragment implements OnItemClickListener,
		OnClickListener {

	private String parentPath = "";
	private String currentPath = "";
	private String currentFolderName = "";
	
	private WebDavManager webDavManager;
	private ArrayList<WebDavItemEntity> webDavItemEntities = new ArrayList<WebDavItemEntity>();
	private ListView fileListView;
	private CloudFileListAdapter cloudFileListAdapter;
	private ProgressBar progressBar;
	private LinearLayout backArrowLayout;
	private TextView currentFolderNameTextView;
	private ImageButton backButton;
	private Menu menu;

	private AlertDialog createFolderAlertDialog;
	private EditText newFolderNameEditText;
	private Button createOKButton;
	private Button createCancelButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater
				.inflate(R.layout.cloud_fragement, container, false);

		backArrowLayout = (LinearLayout) view.findViewById(R.id.cloud_backArrowLayout);
		
		currentFolderNameTextView = (TextView) view.findViewById(R.id.cloud_currentFolderNameTextView);
		backButton = (ImageButton) view.findViewById(R.id.cloud_backButton);
		backButton.setOnClickListener(this);
		
		progressBar = (ProgressBar) view.findViewById(R.id.cloud_progressBar);
		progressBar.setVisibility(View.VISIBLE);

		fileListView = (ListView) view.findViewById(R.id.cloud_listView);
		fileListView.setVisibility(View.GONE);

		cloudFileListAdapter = new CloudFileListAdapter(getActivity(),
				webDavItemEntities);
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

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.cloud_manage_menu, menu);
		this.menu = menu;

		if (StringUtil.isEmpty(currentPath)) {
			showMenu(false);
		} else {
			showMenu(true);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.cloudMenu_createFolder:
			showCreateFolderAlertDialog();

			return true;

		case R.id.cloudMenu_upload:
			Intent intent = new Intent();
			intent.setClass(getActivity().getApplicationContext(),
					LocalFileListActivity.class);
			startActivity(intent);

			return true;

		default:
			break;
		}

		return false;
	}

	private void initWebDav() {
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

	private String getFolderName(String path){
		String folderName = "";
		
		int index = path.lastIndexOf("/");
		folderName = path.substring(index + 1, path.length());
		return folderName;
	}
	
	private void showFileList(final String path) {

		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progressBar.setVisibility(View.VISIBLE);
				fileListView.setVisibility(View.GONE);
				
				String folderName = "";
				
				// add the up arrow icon at the first position.
				if (StringUtil.isEmpty(path)) {
					backArrowLayout.setVisibility(View.GONE);
				} else {
					folderName = getFolderName(path);
					backArrowLayout.setVisibility(View.VISIBLE);
				}
				
				currentFolderNameTextView.setText(folderName);
			}
		});
		
		new Thread(new Runnable() {

			@Override
			public void run() {

				// Translate the path string to UTF-8 string.
				String utf8Path = StringUtil.getWebDavURLEncodeStr(path);

				MultiStatusResponse[] multiStatusResponses = webDavManager
						.getFileList(utf8Path);
				webDavItemEntities.clear();

				ArrayList<WebDavItemEntity> folders = new ArrayList<WebDavItemEntity>();
				ArrayList<WebDavItemEntity> files = new ArrayList<WebDavItemEntity>();

				for (int i = 0; i < multiStatusResponses.length; i++) {

					if (i > 0) { // ignore the first item.

						WebDavItemEntity webDavItemEntity = ganerateDavItemEntity(multiStatusResponses[i]);

						switch (webDavItemEntity.getItemType()) {
						case Folder:
							folders.add(webDavItemEntity);
							break;

						case File:
							files.add(webDavItemEntity);
							break;

						default:
							break;
						}

					}

				}


				Collections.sort(folders, new SortByName()); // Sort by Folder Name
				Collections.sort(files, new SortByName()); // Sort by File Name

				// Merge the folder list and file list.
				webDavItemEntities.addAll(folders);
				webDavItemEntities.addAll(files);

				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						cloudFileListAdapter.notifyDataSetChanged();
						progressBar.setVisibility(View.GONE);
						fileListView.setVisibility(View.VISIBLE);

						if (StringUtil.isEmpty(currentPath)) {
							showMenu(false);
						} else {
							showMenu(true);
						}

					}
				});

			}
		}).start();

	}

	class SortByName implements Comparator<WebDavItemEntity> {

		@Override
		public int compare(WebDavItemEntity lhs, WebDavItemEntity rhs) {
			return lhs.getName().toString()
					.compareToIgnoreCase(rhs.getName().toString());
		}

	}

	private WebDavItemEntity ganerateDavItemEntity(
			MultiStatusResponse multiStatusResponse) {

		WebDavItemEntity webDavItemEntity = new WebDavItemEntity();
		int status = multiStatusResponse.getStatus()[0].getStatusCode();
		DavPropertySet davPropertySet = multiStatusResponse
				.getProperties(status);

		// Get content Type
		DavProperty<?> davProperty = davPropertySet
				.get(DavPropertyName.GETCONTENTTYPE);
		String contentType;
		if (davProperty != null) {
			contentType = (String) davProperty.getValue();
		} else {
			contentType = "None";
		}

		// Get type
		if (contentType.equals("httpd/unix-directory")) {
			webDavItemEntity.setItemType(ItemType.Folder);
		} else {
			webDavItemEntity.setItemType(ItemType.File);
		}// End if

		// Get Create Date
		davProperty = davPropertySet.get(DavPropertyName.PROPERTY_CREATIONDATE);
		String createDate = davProperty.getValue().toString();
		webDavItemEntity.setCreateDate(createDate);

		// Get Last Modify Date
		davProperty = davPropertySet.get(DavPropertyName.GETLASTMODIFIED);
		String modifyDate = davProperty.getValue().toString();
		webDavItemEntity.setModifyDate(modifyDate);

		// Get File Size
		davProperty = davPropertySet.get(DavPropertyName.GETCONTENTLENGTH);
		String size = davProperty.getValue().toString();
		webDavItemEntity.setSize(size);

		// Get file url
		String fileUrl = multiStatusResponse.getHref();
		String fileUrlTemp = fileUrl;

		try {
			fileUrlTemp = java.net.URLDecoder.decode(fileUrlTemp, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}// End try

		String name = "";
		davProperty = davPropertySet.get(DavPropertyName.DISPLAYNAME);
		if (davProperty != null) {
			name = (String) davProperty.getName().toString();
		} else {
			String[] tmp = fileUrlTemp.split("/");
			if (tmp.length > 0) {
				name = tmp[tmp.length - 1];
			}// End if
		}// End if

		webDavItemEntity.setName(name);

		if (webDavItemEntity.getItemType() == ItemType.File) {

			if (FileUtil.isAudio(name)) {
				webDavItemEntity.setFileType(FileType.Audio);

			} else if (FileUtil.isImage(name)) {
				webDavItemEntity.setFileType(FileType.Image);

			} else if (FileUtil.isVideo(name)) {
				webDavItemEntity.setFileType(FileType.Video);

			} else {
				webDavItemEntity.setFileType(FileType.Other);
			}
		}

		// Get Play URL
		String playUrl; // URL with the Username and Password for Player
		String username = ApplicationManager.getInstance().getWebDavUsername();
		String password = ApplicationManager.getInstance().getWebDavPassword();
		String hostName = ApplicationManager.getInstance().getDriveIp();
		String hostPort = ApplicationManager.getInstance().getDrivePort();
		// Video_URL_Tmp =
		// "http://admin:admin@192.168.1.1:8081/SD/Aximcom-sonny.mp4";
		playUrl = "http://" + username + ":" + password + "@" + hostName + ":"
				+ hostPort + "/" + currentPath + "/" + name;
		webDavItemEntity.setPlayUrl(playUrl);
		// Log.d("Mylog", "play url = " + playUrl);

		return webDavItemEntity;
	}

	private void showMenu(final boolean flag) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {

				for (int i = 0; i < menu.size(); i++) {
					menu.getItem(i).setVisible(flag);
				}

			}
		});
	}

	private void showCreateFolderAlertDialog() {
		LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
		View view = layoutInflater.inflate(
				R.layout.cloud_create_folder_form, null);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Create Folder");
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setView(view);

		newFolderNameEditText = (EditText) view.findViewById(R.id.cloud_newFolderNameEditText);
		
		createOKButton = (Button) view.findViewById(R.id.cloud_newFolderNameOKButton);
		createOKButton.setOnClickListener(this);
		
		createCancelButton = (Button) view.findViewById(R.id.cloud_newFolderNameCancelButton);
		createCancelButton.setOnClickListener(this);
		createFolderAlertDialog = builder.create();
		createFolderAlertDialog.show();
	}

	// public ActionMode.Callback actionModeCallback = new ActionMode.Callback()
	// {
	//
	// @Override
	// public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
	// MenuInflater inflater = actionMode.getMenuInflater();
	// inflater.inflate(R.menu.cloud_upload_menu, menu);
	// return true;
	// }
	//
	// @Override
	// public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	// return false; // Return false if nothing is done
	// }
	//
	// @Override
	// public boolean onActionItemClicked(ActionMode actionMode, MenuItem item)
	// {
	// switch (item.getItemId()) {
	// case R.id.cloudMenu_createFolder:
	// Log.d("Mylog", "create folder");
	// // shareCurrentItem();
	// // actionMode.finish(); // Action picked, so close the CAB
	// return true;
	//
	// case R.id.cloudMenu_upload:
	// Log.d("Mylog", "upload");
	// // shareCurrentItem();
	// // actionMode.finish(); // Action picked, so close the CAB
	// return true;
	//
	// default:
	// return false;
	// }
	// }
	//
	// // Called when the user exits the action mode
	// @Override
	// public void onDestroyActionMode(ActionMode mode) {
	// actionMode = null;
	// manageButton.setVisibility(View.VISIBLE);
	// }
	// };

	private void onCreateFolderClick(){
		final String folderName = newFolderNameEditText.getText().toString();
		
		// check the folder name is empty or not.
		if (StringUtil.isEmpty(folderName)) {
			Toast.makeText(getActivity().getApplicationContext(),
					"Please input the new folder name", Toast.LENGTH_LONG).show();
			return;
		}
		
		// Check the folder name exist or not.
		boolean isDuplicate = false;
		for (WebDavItemEntity webDavItemEntity : webDavItemEntities) {
			if (webDavItemEntity.getItemType() == ItemType.Folder) {
				if (folderName.equals(webDavItemEntity.getName())) {
					isDuplicate = true;
				}
			}
		}
		
		if (isDuplicate) {
			Toast.makeText(getActivity().getApplicationContext(),
					"The folder name has existed.", Toast.LENGTH_LONG).show();
			return;
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String path = "";
				String hostName = ApplicationManager.getInstance().getDriveIp();
				String hostPort = ApplicationManager.getInstance().getDrivePort();

				String folderNameTmp = folderName;
				
				try {
					folderNameTmp = URLEncoder.encode(folderNameTmp,"UTF-8");
					folderNameTmp = folderNameTmp.replace("+", "%20");	// replace %20 with +
					
					if (StringUtil.isEmpty(currentPath) ) {
						path = "http://" + hostName + ":" + hostPort + "/"
								+ folderNameTmp;
					} else {
						path = "http://" + hostName + ":" + hostPort + "/"
								+ currentPath + "/" + folderNameTmp;
					}
					
					WebDavManager.getInstance().createNewFolder(path);
					showFileList(currentPath);

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
			}
		}).start();
		
		newFolderNameEditText.setText("");
		createFolderAlertDialog.dismiss();
		
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		ItemType type = webDavItemEntities.get(position).getItemType();
		String itemName = webDavItemEntities.get(position).getName();

		switch (type) {

		case Folder:
			currentFolderName = itemName;

			parentPath = currentPath;
			currentPath += "/" + itemName;
			showFileList(currentPath);
			break;

		case File:

			
			break;

		default:
			break;
		}

		Log.d("Mylog", "parentPath=" + parentPath);
		Log.d("Mylog", "currentPath=" + currentPath);

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.cloud_newFolderNameOKButton:
			onCreateFolderClick();
			
			break;

		case R.id.cloud_newFolderNameCancelButton:
			newFolderNameEditText.setText("");
			createFolderAlertDialog.dismiss();
			break;

		case R.id.cloud_backButton:
			
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
	}

}
