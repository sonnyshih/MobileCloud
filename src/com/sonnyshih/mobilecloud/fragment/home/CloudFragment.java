package com.sonnyshih.mobilecloud.fragment.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;

import com.sonnyshih.mobilecloud.R;
import com.sonnyshih.mobilecloud.activity.home.MainActivity;
import com.sonnyshih.mobilecloud.activity.localfile.LocalFileListActivity;
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
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CloudFragment extends BaseFragment implements OnItemClickListener,
		OnClickListener, MultiChoiceModeListener {

	public static String BUNDLE_STRING_CURRENT_PATH = "BUNDLE_STRING_CURRENT_PATH";
	public static String BUNDLE_ARRAYLIST_WEBDAV_ITEM_ENTITIES = "BUNDLE_ARRAYLIST_WEBDAV_ITEM_ENTITIES";
	
	private String parentPath = "";
	private String currentPath = "";
	
	private WebDavManager webDavManager;
	private ArrayList<WebDavItemEntity> webDavItemEntities = new ArrayList<WebDavItemEntity>();
	private ListView fileListView;
	private CloudFileListAdapter cloudFileListAdapter;
	private ProgressBar progressBar;
	
	private ArrayList<WebDavItemEntity> selectedWebDavItemEntities;
	private LinearLayout copyAndMoveLayout;
	private Button cancelButton;
	private Button moveHereButton;
	private Button copyHereButton;
	private LinearLayout backArrowLayout;
	private TextView currentFolderNameTextView;
	private ImageButton backButton;
	private Menu menu;

	private AlertDialog createFolderAlertDialog;
	private EditText newFolderNameEditText;
	private Button createOKButton;
	private Button createCancelButton;
	private ActionMode actionMode;
	private boolean isStopCopy = false;
	private boolean isStopMove = false;
	private boolean isStopDelete = false;
	private Thread copyWebDavItemThread;
	private Thread moveWebDavItemThread;
	private Thread deleteWebDavItemThread;
	private AlertDialog handleFileProgressAlertDialog;
	private ProgressBar handleProgressBar;
	private int currentNumber = 0;
	private TextView currentFileNameTextView;
	private TextView currentNumberTextView;
	private TextView totalTextView;

	
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

		copyAndMoveLayout = (LinearLayout) view.findViewById(R.id.cloud_copyAndMoveLayout);
		cancelButton = (Button) view.findViewById(R.id.cloud_cancelButton);
		cancelButton.setOnClickListener(this);
		moveHereButton = (Button) view.findViewById(R.id.cloud_moveHereButton);
		moveHereButton.setOnClickListener(this);
		copyHereButton = (Button) view.findViewById(R.id.cloud_copyHereButton);
		copyHereButton.setOnClickListener(this);
		
		
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
		fileListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		fileListView.setMultiChoiceModeListener(this);

		
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
		inflater.inflate(R.menu.cloud_menu, menu);
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
			
			Bundle bundle = new Bundle();
			bundle.putString(BUNDLE_STRING_CURRENT_PATH, currentPath);
			bundle.putSerializable(BUNDLE_ARRAYLIST_WEBDAV_ITEM_ENTITIES, webDavItemEntities);  
			intent.putExtras(bundle);
			
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
					copyAndMoveLayout.setVisibility(View.GONE);
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
				String utf8Path = StringUtil.pathEncodeURL(path);

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
		webDavItemEntity.setUrl(fileUrl);
		
		String fileUrlTemp = StringUtil.decodeURL(fileUrl) ;

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
		String host = ApplicationManager.getInstance().getDriveIp();
		String port = ApplicationManager.getInstance().getDrivePort();
		// Video_URL_Tmp =
		// "http://admin:admin@192.168.1.1:8081/SD/Aximcom-sonny.mp4";
		
		String encodePath = "";
		String encodeName = "";
		encodePath = StringUtil.pathEncodeURL(currentPath);
		encodeName = StringUtil.encodeURL(name);
		playUrl = "http://" + username + ":" + password + "@" + host + ":"
				+ port + encodePath + "/" + encodeName;
		webDavItemEntity.setPlayUrl(playUrl);
		
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
				String host = ApplicationManager.getInstance().getDriveIp();
				String port = ApplicationManager.getInstance().getDrivePort();

				String folderNameTmp = folderName;
				String currentPathTmp = currentPath;
				folderNameTmp = StringUtil.encodeURL(folderNameTmp);
				

				currentPathTmp = StringUtil.pathEncodeURL(currentPathTmp);
				
				if (StringUtil.isEmpty(currentPath) ) {
					path = "http://" + host + ":" + port + "/"
							+ folderNameTmp;
				} else {
					path = "http://" + host + ":" + port
							+ currentPathTmp + "/" + folderNameTmp;
				}
				
				WebDavManager.getInstance().createNewFolder(path);
				showFileList(currentPath);

			}
		}).start();
		
		newFolderNameEditText.setText("");
		createFolderAlertDialog.dismiss();
		
	}
	
	
	private boolean isHaveSameFolderPath(ArrayList<WebDavItemEntity> webDavItemEntities, String currentPath){
		boolean isSame = false;
		
		String host = ApplicationManager.getInstance().getDriveIp();
		String port = ApplicationManager.getInstance().getDrivePort();

		String path = "http://" + host + ":" + port + currentPath;
		
		for (WebDavItemEntity webDavItemEntity : webDavItemEntities) {
			String decodeUrl = StringUtil.decodeURL(webDavItemEntity.getUrl());
			
			if (path.equals(decodeUrl)) {
				isSame = true;
			}

		}
		
		return isSame;
	}
	
	private void copyWebDaveItem(final ArrayList<WebDavItemEntity> copyWebDavItemEntities){

		// Can not copy self folder into self folder.
		boolean isHaveSameFolderPath = isHaveSameFolderPath(copyWebDavItemEntities, currentPath);
		if (isHaveSameFolderPath) {
			isStopCopy = true;
			Toast.makeText(getActivity().getApplicationContext(),
					"Can not copy self folder into self folder...", Toast.LENGTH_LONG).show();
			return;
		}
		
		showHandleWebDavItemProgressDialog("Coping files...");
		currentNumber = 0;
		totalTextView.setText(Integer.toString(copyWebDavItemEntities.size()));
		handleProgressBar.setMax(copyWebDavItemEntities.size());
		isStopCopy = false;
		
		copyWebDavItemThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				handleProgressBar.setProgress(0);

				for (WebDavItemEntity copyWebDavItemEntity : copyWebDavItemEntities) {
					
					// Stop copy 
					if (isStopCopy) {
						return;
					}
					
					final String webDavItemName = copyWebDavItemEntity.getName();
					
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							currentNumber++;
							currentNumberTextView.setText(Integer.toString(currentNumber));
							currentFileNameTextView.setText(webDavItemName);
						}
					});

					handleProgressBar.setProgress(currentNumber);
					
					String destinationName = webDavItemName;

					for (WebDavItemEntity webDavItemEntity : webDavItemEntities) {
						if (webDavItemName.equals(webDavItemEntity.getName())) {
							destinationName = "copy_" + webDavItemName;
						}
					}
					
					WebDavManager.getInstance().copyWebDavItem(
							copyWebDavItemEntity.getUrl(), currentPath,
							destinationName);
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}

				
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						cloudFileListAdapter.cleanAllSelected();
						dismissHandleWebDavItemProgressDialog();
					}
				});

			}
		});		
		
		copyWebDavItemThread.start();
		
	}
	
	
	
	private void moveWebDaveItem(final ArrayList<WebDavItemEntity> moveWebDavItemEntities){
		
		// Can not move self folder into self folder.
		boolean isHaveSameFolderPath = isHaveSameFolderPath(moveWebDavItemEntities, currentPath);
		if (isHaveSameFolderPath) {
			isStopMove = true;
			Toast.makeText(getActivity().getApplicationContext(),
					"Can not move self folder into self folder...", Toast.LENGTH_LONG).show();
			return;
		}

		
		showHandleWebDavItemProgressDialog("Moving files...");
		currentNumber = 0;
		totalTextView.setText(Integer.toString(moveWebDavItemEntities.size()));
		handleProgressBar.setMax(moveWebDavItemEntities.size());
		isStopMove = false;
		
		moveWebDavItemThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				handleProgressBar.setProgress(0);
				
				for (WebDavItemEntity moveWebDavItemEntity : moveWebDavItemEntities) {
					
					// Stop delete 
					if (isStopMove) {
						return;
					}
					
					final String webDavItemName = moveWebDavItemEntity.getName();
					
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							currentNumber++;
							currentNumberTextView.setText(Integer.toString(currentNumber));
							currentFileNameTextView.setText(webDavItemName);
						}
					});

					handleProgressBar.setProgress(currentNumber);
					
					String destinationName = webDavItemName;

					for (WebDavItemEntity webDavItemEntity : webDavItemEntities) {
						if (webDavItemName.equals(webDavItemEntity.getName())) {
							destinationName = "move_" + webDavItemName;
						}
					}
					
					WebDavManager.getInstance().moveWebDavItem(
							moveWebDavItemEntity.getUrl(), currentPath,
							destinationName);
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}

				
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						cloudFileListAdapter.cleanAllSelected();
						dismissHandleWebDavItemProgressDialog();
					}
				});

			}
		});		
		
		moveWebDavItemThread.start();
		
	}
	
	private void deleteWebDaveItem(final ArrayList<WebDavItemEntity> deleteWebDavItemEntities){
		showHandleWebDavItemProgressDialog("Deleting files...");
		currentNumber = 0;
		totalTextView.setText(Integer.toString(deleteWebDavItemEntities.size()));
		handleProgressBar.setMax(deleteWebDavItemEntities.size());
		isStopDelete = false;
		
		deleteWebDavItemThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				handleProgressBar.setProgress(0);
				
				for (WebDavItemEntity webDavItemEntity : deleteWebDavItemEntities) {
					
					// Stop delete 
					if (isStopDelete) {
						return;
					}
					
					final String fileName = webDavItemEntity.getName();
					
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							currentNumber++;
							currentNumberTextView.setText(Integer.toString(currentNumber));
							currentFileNameTextView.setText(fileName);
						}
					});

					handleProgressBar.setProgress(currentNumber);
					WebDavManager.getInstance().deleteWebDavItem(webDavItemEntity.getUrl());
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}

				
				if (actionMode != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							cloudFileListAdapter.cleanAllSelected();
							actionMode.finish();
							dismissHandleWebDavItemProgressDialog();
						}
					});
				}

			}
		});		
		
		deleteWebDavItemThread.start();

	}
	
	private void showHandleWebDavItemProgressDialog(String dialogTitle){
		LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
		View handleFileView = layoutInflater.inflate(R.layout.handle_file_progress_dialog, null);
		
		
		currentFileNameTextView = (TextView) handleFileView
				.findViewById(R.id.handleFileProgressDialog_currentFileNameTextView);
		handleProgressBar = (ProgressBar) handleFileView
				.findViewById(R.id.handleFileProgressDialog_progressBar);
		currentNumberTextView = (TextView) handleFileView
				.findViewById(R.id.handleFileProgressDialog_currentNumberTextView);
		totalTextView = (TextView) handleFileView
				.findViewById(R.id.handleFileProgressDialog_totalTextView);
		
		AlertDialog.Builder webDavItemBuilder = new AlertDialog.Builder(getActivity());
		webDavItemBuilder.setTitle(dialogTitle);
		webDavItemBuilder.setIcon(android.R.drawable.ic_menu_info_details);
		webDavItemBuilder.setView(handleFileView);
		webDavItemBuilder.setCancelable(false);
		
		// Setting Middle Button
		webDavItemBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismissHandleWebDavItemProgressDialog();
            }
        });
		
		handleFileProgressAlertDialog = webDavItemBuilder.create();
		handleFileProgressAlertDialog.show();

	}

	private void dismissHandleWebDavItemProgressDialog(){
		
		cloudFileListAdapter.cleanAllSelected();

		isStopCopy = true;
		// Abort coping files.
		if (WebDavManager.getInstance().getCopyMethod() != null) {
			WebDavManager.getInstance().getCopyMethod().abort();
			WebDavManager.getInstance().setCopyMethod(null);
		}
		
		isStopMove = true;
		// Abort moving files.
		if (WebDavManager.getInstance().getMoveMethod() !=null) {
			WebDavManager.getInstance().getMoveMethod().abort();
			WebDavManager.getInstance().setMoveMethod(null);
		}
		
		isStopDelete = true;
		// Abort deleting files.
		if (WebDavManager.getInstance().getDeleteMethod() != null) {
			WebDavManager.getInstance().getDeleteMethod().abort();
			WebDavManager.getInstance().setDeleteMethod(null);
		}
		
		if (actionMode != null) {
			actionMode.finish();
		}
		
		if (handleFileProgressAlertDialog.isShowing()) {
			handleFileProgressAlertDialog.dismiss();
		}
		
		showFileList(currentPath);
	}
	
	
	private void onActionModeCopyClick(){
		
		selectedWebDavItemEntities = getSelectedWebDavItemEntities();
		
		copyAndMoveLayout.setVisibility(View.VISIBLE);
		copyHereButton.setVisibility(View.VISIBLE);
		moveHereButton.setVisibility(View.GONE);
		actionMode.finish();
	}
	
	private void onCopyHereClick(){
		copyAndMoveLayout.setVisibility(View.GONE);
		copyWebDaveItem(selectedWebDavItemEntities);
		
	}
	
	private void onActionModeMoveClick(){
		selectedWebDavItemEntities = getSelectedWebDavItemEntities();

		copyAndMoveLayout.setVisibility(View.VISIBLE);
		copyHereButton.setVisibility(View.GONE);
		moveHereButton.setVisibility(View.VISIBLE);
		actionMode.finish();
		
	}
	
	private void onMoveHereClick(){
		copyAndMoveLayout.setVisibility(View.GONE);
		moveWebDaveItem(selectedWebDavItemEntities);
	}
	
	
	private void onActionModeDeleteClick(){
		selectedWebDavItemEntities = getSelectedWebDavItemEntities();
		deleteWebDaveItem(selectedWebDavItemEntities);
	}
	
	
	private ArrayList<WebDavItemEntity> getSelectedWebDavItemEntities(){
		ArrayList<WebDavItemEntity> selectedWebDavItemEntities = new ArrayList<WebDavItemEntity>();
		ArrayList<WebDavItemEntity> webDavItemEntities = cloudFileListAdapter.getWebDavItemEntities();
		
		for (WebDavItemEntity webDavItemEntity : webDavItemEntities) {
			if (webDavItemEntity.isChecked()) {
				selectedWebDavItemEntities.add(webDavItemEntity);
			}
		}
		
		return selectedWebDavItemEntities;
	}
	
	public void closeActionMode(){
		if (actionMode != null) {
			actionMode.finish();
		}
	}
	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		ItemType type = webDavItemEntities.get(position).getItemType();
		String itemName = webDavItemEntities.get(position).getName();

		switch (type) {

		case Folder:

			parentPath = currentPath;
			currentPath += "/" + itemName;
			showFileList(currentPath);
			break;

		case File:

			
			break;

		default:
			break;
		}

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
			
			if (actionMode != null) {
				actionMode.finish();
			}
			
			showFileList(parentPath);
			currentPath = parentPath;

			// File's parent path
			if (!currentPath.equals("")) {
				int index = currentPath.lastIndexOf("/");
				parentPath = currentPath.substring(0, index);
			}
			
			break;
			
						
		case R.id.cloud_cancelButton:
			copyAndMoveLayout.setVisibility(View.GONE);
			break;

		case R.id.cloud_copyHereButton:
			onCopyHereClick();
			break;
			
		case R.id.cloud_moveHereButton:
			onMoveHereClick();
			break;
			
		default:
			break;
		}
	}

	@Override
	public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.cloud_manage_menu, menu);
        this.actionMode = actionMode;

		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
		case R.id.cloudManageMenu_copy:
			onActionModeCopyClick();
			return true;
			
		case R.id.cloudManageMenu_move:
			onActionModeMoveClick();
			return true;
			
		case R.id.cloudManageMenu_delete:
			onActionModeDeleteClick();
			return true;
			
		default:
			return false;
		}
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		cloudFileListAdapter.cleanAllSelected();
		actionMode = null;
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean isChecked) {

		if (isChecked) {
			cloudFileListAdapter.selected(position);
		} else {
			cloudFileListAdapter.unselected(position);
		}

	}

}
