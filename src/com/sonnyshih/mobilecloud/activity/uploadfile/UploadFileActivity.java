package com.sonnyshih.mobilecloud.activity.uploadfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.sonnyshih.mobilecloud.R;
import com.sonnyshih.mobilecloud.base.BaseFragmentActivity;
import com.sonnyshih.mobilecloud.entity.ActionModeFileEntity;
import com.sonnyshih.mobilecloud.entity.WebDavItemEntity;
import com.sonnyshih.mobilecloud.fragment.home.CloudFragment;
import com.sonnyshih.mobilecloud.manage.ApplicationManager;
import com.sonnyshih.mobilecloud.ui.adapter.LocalFileListAdapter;
import com.sonnyshih.mobilecloud.util.FileUtil;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UploadFileActivity extends BaseFragmentActivity implements
		OnClickListener, OnItemClickListener, MultiChoiceModeListener,
		ServiceConnection {
	
	private Button cancelButton;
	private ListView listView;
	private LocalFileListAdapter localFileListAdapter;
	
	private ArrayList<ActionModeFileEntity> fileArrayList = new ArrayList<ActionModeFileEntity>();
	private File currentFileOrFolder = null;
	private LinearLayout backArrowLayout;
	private ImageView backImageView;
	private TextView currentFolderNameTextView;
	private ActionMode actionMode;
	
	private String currentPath;
	private ArrayList<WebDavItemEntity> webDavItemEntities;
	private AlertDialog uploadProgressAlertDialog;
	
	private TextView currentFileNameTextView;
	private TextView currentNumberTextView;
	private TextView totalTextView;
	private ProgressBar progressBar;
	
	private String webDavServiceClassName = WebDaveService.class.getName();
	private WebDaveService webDaveService;
	
	@Override
	protected void onStart() {
		super.onStart();
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		currentPath = bundle.getString(CloudFragment.BUNDLE_STRING_CURRENT_PATH);
		webDavItemEntities = (ArrayList<WebDavItemEntity>) bundle
				.getSerializable(CloudFragment.BUNDLE_ARRAYLIST_WEBDAV_ITEM_ENTITIES);
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local_file_list_activity);
		
		backArrowLayout = (LinearLayout) findViewById(R.id.localFileList_backArrowLayout);
		backImageView = (ImageView) findViewById(R.id.localFileList_backImageView);
		backImageView.setOnClickListener(this);
		currentFolderNameTextView = (TextView) findViewById(R.id.localFileList_currentFolderNameTextView);
		
		cancelButton = (Button) findViewById(R.id.localFileList_cancelButton);
		cancelButton.setOnClickListener(this);
		
		listView = (ListView) findViewById(R.id.localFileList_listView);
		localFileListAdapter = new LocalFileListAdapter(this, fileArrayList);
		listView.setAdapter(localFileListAdapter);
		listView.setOnItemClickListener(this);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(this);
		
		getForderAndFileList(null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		startWebDaveService();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	private void showBackArrowLayout(){
		
		if (currentFileOrFolder != null
				&& !currentFileOrFolder.getPath().equals(FileUtil.getStorageRootPath())) {
			backArrowLayout.setVisibility(View.VISIBLE);
			currentFolderNameTextView.setText(currentFileOrFolder.getName());
		} else {
			backArrowLayout.setVisibility(View.GONE);
			currentFolderNameTextView.setText("");
		}
		
	}
	
	private void getForderAndFileList(File file){
		String path;
		if (file == null) {
			path = FileUtil.getStorageRootPath();
		} else {
			path = file.getPath();
			currentFileOrFolder = file;
		} 
		
		fileArrayList.clear();
		
		File fileTemp = new File(path);
		File[] fileList = fileTemp.listFiles();	// List all files of the directory
		
		ArrayList<ActionModeFileEntity> folders = new ArrayList<ActionModeFileEntity>();
		ArrayList<ActionModeFileEntity> files = new ArrayList<ActionModeFileEntity>();
		
		for (File fileItem : fileList) {
			
			ActionModeFileEntity actionModeFileEntity =  new ActionModeFileEntity();
			actionModeFileEntity.setFile(fileItem);
			
			if (fileItem.isDirectory()) {
				folders.add(actionModeFileEntity);
			} else {
				files.add(actionModeFileEntity);
			}
		}
		
		Collections.sort(folders, new SortByName()); // Sort by Folder Name
		Collections.sort(files, new SortByName()); // Sort by Folder Name
		fileArrayList.addAll(folders);
		fileArrayList.addAll(files);
		
		localFileListAdapter.notifyDataSetChanged();
		listView.setAdapter(localFileListAdapter);
		showBackArrowLayout();
	}
	
	class SortByName implements Comparator<ActionModeFileEntity> {

		@Override
		public int compare(ActionModeFileEntity lhs, ActionModeFileEntity rhs) {
			return lhs.getFile().getName().toString()
					.compareToIgnoreCase(rhs.getFile().getName().toString());
		}

	}

	
	private void uploadFile(final ArrayList<ActionModeFileEntity> uploadFileArrayList){
		
		ShowUploadFileProgressDialog();

		webDaveService.startUpload(currentPath, uploadFileArrayList);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				while (!webDaveService.isStopUpload()) {
					
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							totalTextView.setText(Integer
									.toString(webDaveService
											.getUploadFileArrayList().size()));

							currentNumberTextView.setText(Integer
									.toString(webDaveService
											.getCurrentNumber()));

							currentFileNameTextView.setText(webDaveService
									.getCurrentUploadFile().getName());

						}
					});

					progressBar.setMax((int) webDaveService
							.getCurrentUploadFile().length());
					
					progressBar.setProgress(webDaveService.getUploadFileProgress());
					
					
				}
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dismissUploadFileProgressDialog();
					}
				});				
				
			}
		}).start();
		
	}

	private boolean isExsitOnWebDav(String fileName){
		boolean flag = false;
		
		for (WebDavItemEntity webDavItemEntity : webDavItemEntities) {
			if (fileName.equals(webDavItemEntity.getName())) {
				flag = true;
			}
		}
		
		return flag;
		
	}

	
	private void ShowUploadFileProgressDialog(){
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View uploadFileView = layoutInflater.inflate(R.layout.handle_file_progress_dialog, null);
		
		currentFileNameTextView = (TextView) uploadFileView
				.findViewById(R.id.handleFileProgressDialog_currentFileNameTextView);
		progressBar = (ProgressBar) uploadFileView
				.findViewById(R.id.handleFileProgressDialog_progressBar);
		currentNumberTextView = (TextView) uploadFileView
				.findViewById(R.id.handleFileProgressDialog_currentNumberTextView);
		totalTextView = (TextView) uploadFileView
				.findViewById(R.id.handleFileProgressDialog_totalTextView);
		
		AlertDialog.Builder uploadFileBuilder = new AlertDialog.Builder(this);
		uploadFileBuilder.setTitle("Uploading...");
		uploadFileBuilder.setIcon(android.R.drawable.ic_menu_info_details);
		uploadFileBuilder.setView(uploadFileView);
		uploadFileBuilder.setCancelable(false);
		
		// Setting Middle Button
		uploadFileBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismissUploadFileProgressDialog();
            }
        });
		
		
		uploadProgressAlertDialog = uploadFileBuilder.create();
		uploadProgressAlertDialog.show();

	}
	
	private void dismissUploadFileProgressDialog(){
		
		webDaveService.stopUpload();
		
		localFileListAdapter.cleanAllSelected();
		if (actionMode != null) {
			actionMode.finish();
		}
		
		if (uploadProgressAlertDialog.isShowing()) {
			uploadProgressAlertDialog.dismiss();
		}
	}
	
	
	private void startWebDaveService(){
		Intent intent = new Intent(this, WebDaveService.class);
		
		if (!ApplicationManager.getInstance().isServiceRunning(
				webDavServiceClassName)) {
			startService(intent);
		}
		
		bindService(intent, this, Context.BIND_AUTO_CREATE);

	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		final File file = fileArrayList.get(position).getFile();
		
		if (file.isDirectory()) {
			getForderAndFileList(file);
		}
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.localFileList_cancelButton:
			finish();
			break;

		case R.id.localFileList_backImageView:
			if (actionMode != null) {
				actionMode.finish();
			}
			
			getForderAndFileList(currentFileOrFolder.getParentFile());
			break;
			
		default:
			break;
		}
	}

	@Override
	public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.cloud_upload_menu, menu);
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
		case R.id.cloudMenu_upload:
			
			ArrayList<ActionModeFileEntity> uploadFileArrayList = new ArrayList<ActionModeFileEntity>();
			
			ArrayList<ActionModeFileEntity> fileArrayList = localFileListAdapter
					.getActionModeFileEntities();
			
			for (ActionModeFileEntity actionModeFileEntity : fileArrayList) {
				
				if (actionModeFileEntity.isChecked()){
					
					boolean isExsitOnWebDav = isExsitOnWebDav(actionModeFileEntity
							.getFile().getName());
					actionModeFileEntity.setExsitOnWebDav(isExsitOnWebDav);
					
					uploadFileArrayList.add(actionModeFileEntity);
				}
			}
			
			uploadFile(uploadFileArrayList);
			
			return true;
			
		default:
			return false;
		}

	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		localFileListAdapter.cleanAllSelected();
		actionMode = null;
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean isChecked) {

		if (fileArrayList.get(position).getFile().isFile()) {
			if (isChecked) {
				localFileListAdapter.selected(position);
			} else {
				localFileListAdapter.unselected(position);
			}
		}

	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		webDaveService = ((WebDaveService.ServiceBinder) service).getService();

	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		webDaveService = null;
	}
	
}
