package com.sonnyshih.mobilecloud.ui.adapter;

import java.util.ArrayList;

import com.sonnyshih.mobilecloud.R;
import com.sonnyshih.mobilecloud.entity.ActionModeFileEntity;
import com.sonnyshih.mobilecloud.entity.WebDavItemEntity;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CloudFileListAdapter extends BaseAdapter {
	private Context context;

	private ArrayList<WebDavItemEntity> webDavItemEntities;

	public CloudFileListAdapter(Context context, ArrayList<WebDavItemEntity> webDavItemEntities) {
		this.context = context;
		this.webDavItemEntities = webDavItemEntities;
	}

	public ArrayList<WebDavItemEntity> getChartletList() {
		return webDavItemEntities;
	}

	@Override
	public int getCount() {
		return webDavItemEntities.size();
	}

	@Override
	public Object getItem(int position) {
		return webDavItemEntities.get(position);
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
			convertView = infalInflater.inflate(R.layout.cloud_adapter, null);

			
			RelativeLayout itemLayout = (RelativeLayout) convertView
					.findViewById(R.id.cloud_itemLayout);
			
			ImageView fileTypeImageView = (ImageView) convertView
					.findViewById(R.id.cloud_fileTypeImageView);
			
			TextView fileNameTextView = (TextView) convertView.findViewById(R.id.cloud_fileNameTextView);
			
			viewHolder = new ViewHolder(itemLayout, fileTypeImageView, fileNameTextView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		int id;
		
		switch (webDavItemEntities.get(position).getItemType()) {
		case Folder:
			id = R.drawable.ic_folder;
			break;

		case File:
			
			switch (webDavItemEntities.get(position).getFileType()) {
			case Audio:
				id = R.drawable.ic_audio_file;
				break;

			case Image:
				id = R.drawable.ic_photo_file;
				break;

			case Video:
				id = R.drawable.ic_video_file;
				break;

			default:
				id = R.drawable.ic_file;
				break;
			}
			
			break;

		default:
			id = R.drawable.ic_file;
			break;
		}
		
		
		viewHolder.imageView.setImageResource(id);
		viewHolder.fileNameTextView.setText(webDavItemEntities.get(position).getName());
		
		Resources resources = context.getResources();
		// default color
		viewHolder.itemLayout.setBackgroundResource(R.drawable.cloud_adapter_style); 
//		viewHolder.itemLayout.setBackgroundColor(resources
//				.getColor(android.R.color.background_light));
//
		// If item is checked, set the color.
		if (webDavItemEntities.get(position).isChecked()) {
			viewHolder.itemLayout.setBackgroundColor(resources
					.getColor(android.R.color.holo_blue_light));
		}

		
		return convertView;
	}

	public ArrayList<WebDavItemEntity> getWebDavItemEntities(){
		return webDavItemEntities;
	}
	
	public void selected(int position){
		webDavItemEntities.get(position).setChecked(true);
		notifyDataSetChanged();
	}
	
	public void unselected(int position){
		webDavItemEntities.get(position).setChecked(false);
		notifyDataSetChanged();
	}

	
	public void selectAll(){
		for (WebDavItemEntity webDavItemEntity : webDavItemEntities) {
			webDavItemEntity.setChecked(true);
		}
		notifyDataSetChanged();
	}
	
	
	public void cleanAllSelected(){
		for (WebDavItemEntity webDavItemEntity : webDavItemEntities) {
			webDavItemEntity.setChecked(false);
		}
		notifyDataSetChanged();
	}
	
	private class ViewHolder {
		private RelativeLayout itemLayout;
		private ImageView imageView;
		private TextView fileNameTextView;

		private ViewHolder(RelativeLayout itemLayout, ImageView imageView, TextView fileNameTextView) {
			this.itemLayout = itemLayout;
			this.imageView = imageView;
			this.fileNameTextView = fileNameTextView;
		}

	}

}
