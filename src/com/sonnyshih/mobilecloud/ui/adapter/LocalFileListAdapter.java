package com.sonnyshih.mobilecloud.ui.adapter;

import java.io.File;
import java.util.ArrayList;

import com.sonnyshih.mobilecloud.R;
import com.sonnyshih.mobilecloud.entity.ActionModeFileEntity;
import com.sonnyshih.mobilecloud.util.FileUtil;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LocalFileListAdapter extends BaseAdapter{
	private Context context;
	
	private ArrayList<ActionModeFileEntity> fileArrayList = new ArrayList<ActionModeFileEntity>();
	
	public LocalFileListAdapter(Context context, ArrayList<ActionModeFileEntity> fileArrayList){
		this.context = context;
		this.fileArrayList = fileArrayList;
	}

	@Override
	public int getCount() {
		return fileArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return fileArrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder;
		
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.local_file_list_adapter,
					null);
			LinearLayout itemLayout = (LinearLayout) convertView.findViewById(R.id.localFileList_itemLayout);
			ImageView imageView = (ImageView) convertView.findViewById(R.id.localFileList_imageView);
			TextView fileNameTextView = (TextView) convertView.findViewById(R.id.localFileList_fileNameTextView);
			TextView filePathTextView = (TextView) convertView.findViewById(R.id.localFileList_filePathTextView);
			
			viewHolder = new ViewHolder(itemLayout, imageView, fileNameTextView, filePathTextView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		File file = fileArrayList.get(position).getFile();
		boolean isDirectory = file.isDirectory();
		String fileName = file.getName();
		String filePaht = file.getPath();
		
		
		int id = R.drawable.ic_file;
		
		if (isDirectory) {
			id = R.drawable.ic_folder;
		} else {
			
			if (FileUtil.isImage(fileName)) {
				id = R.drawable.ic_photo_file;
				
			} else if (FileUtil.isAudio(fileName)) {
				id = R.drawable.ic_audio_file;
				
			} else if (FileUtil.isVideo(fileName)){
				id = R.drawable.ic_video_file;

			}
		}

		viewHolder.imageView.setImageResource(id);
		
		Resources resources = context.getResources();
		
		// default color
		viewHolder.itemLayout.setBackgroundResource(R.drawable.cloud_adapter_style); 
//		viewHolder.itemLayout.setBackgroundColor(resources
//				.getColor(android.R.color.background_light));
//
		// If item is checked, set the color.
		if (fileArrayList.get(position).isChecked()) {
			viewHolder.itemLayout.setBackgroundColor(resources
					.getColor(android.R.color.holo_blue_light));
		}
		
		viewHolder.fileNameTextView.setText(fileName);
		viewHolder.filePathTextView.setText(filePaht);
		
		return convertView;
	}

	public ArrayList<ActionModeFileEntity> getActionModeFileEntities(){
		return fileArrayList;
	}
	
	public void selected(int position){
		fileArrayList.get(position).setChecked(true);
		notifyDataSetChanged();
	}
	
	public void unselected(int position){
		fileArrayList.get(position).setChecked(false);
		notifyDataSetChanged();
	}

	
	public void selectAll(){
		for (ActionModeFileEntity actionModeFileEntity : fileArrayList) {
			actionModeFileEntity.setChecked(true);
		}
		notifyDataSetChanged();
	}
	
	
	public void cleanAllSelected(){
		for (ActionModeFileEntity actionModeFileEntity : fileArrayList) {
			actionModeFileEntity.setChecked(false);
		}
		notifyDataSetChanged();
	}
	
	private class ViewHolder {
		private LinearLayout itemLayout;
		private ImageView imageView;
		private TextView fileNameTextView;
		private TextView filePathTextView;
		

		private ViewHolder(LinearLayout itemLayout,
				ImageView imageView, TextView fileNameTextView,
				TextView filePathTextView) {

			this.itemLayout = itemLayout;
			this.imageView = imageView;
			this.fileNameTextView = fileNameTextView;
			this.filePathTextView = filePathTextView;

		}

	}

}
