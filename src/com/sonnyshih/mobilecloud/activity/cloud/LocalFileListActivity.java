package com.sonnyshih.mobilecloud.activity.cloud;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.sonnyshih.mobilecloud.R;
import com.sonnyshih.mobilecloud.base.BaseFragmentActivity;
import com.sonnyshih.mobilecloud.entity.ActionModeFileEntity;
import com.sonnyshih.mobilecloud.ui.adapter.LocalFileListAdapter;
import com.sonnyshih.mobilecloud.util.FileUtil;

import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
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
import android.widget.TextView;

public class LocalFileListActivity extends BaseFragmentActivity implements
		OnClickListener, OnItemClickListener, MultiChoiceModeListener {
	
	private Button cancelButton;
	private ListView listView;
	private LocalFileListAdapter localFileListAdapter;
	
	private ArrayList<ActionModeFileEntity> fileArrayList = new ArrayList<ActionModeFileEntity>();
	private File currentFileOrFolder = null;
	private LinearLayout backArrowLayout;
	private ImageView backImageView;
	private TextView currentFolderNameTextView;
	private ActionMode actionMode;
	
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

//	private byte[] getFileBytes(File file) throws IOException {
//		BufferedInputStream bis = null;
//		try {
//			bis = new BufferedInputStream(new FileInputStream(file));
//			int bytes = (int) file.length();
//			byte[] buffer = new byte[bytes];
//			int readBytes = bis.read(buffer);
//			if (readBytes != buffer.length) {
//				throw new IOException("Entire file not read");
//			}
//			return buffer;
//		} finally {
//			if (bis != null) {
//				bis.close();
//			}
//		}
//	}
	
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		File file = fileArrayList.get(position).getFile();
		if (file.isDirectory()) {
			getForderAndFileList(file);
		} else {
//			if (FileTypeUtil.isImage(file.getName())) {
//				if (!xmppTcpConnection.isConnected()) {
//					showErrorAlertDialog(getString(R.string.change_photo_can_not_connect_to_server));
//					return;
//				}
//
//				updatePhoto(file);
//				finish();
//			}
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
		Log.d("Mylog", "asdfas");
//        switch (item.getItemId()) {
//		case R.id.contextMenu_cart:
//			onCartClick();
//			itemCounter = 0;
//			actionModeListAdapter.cleanAllSelected();
//			actionMode.finish(); // Action picked, so close the CAB
//			return true;
//			
//		default:
//			return false;
//		}

		return false;
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
	
}