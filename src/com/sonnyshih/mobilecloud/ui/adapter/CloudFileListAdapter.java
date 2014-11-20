package com.sonnyshih.mobilecloud.ui.adapter;

import java.util.ArrayList;

import com.sonnyshih.mobilecloud.R;
import com.sonnyshih.mobilecloud.entity.WebDavItemEntity;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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

			ImageView fileTypeImageView = (ImageView) convertView
					.findViewById(R.id.cloud_fileTypeImageView);
			
			TextView fileNameTextView = (TextView) convertView.findViewById(R.id.cloud_fileNameTextView);
			
			viewHolder = new ViewHolder(fileTypeImageView, fileNameTextView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		Resources resources = context.getResources();
		String iconName;
		
		switch (webDavItemEntities.get(position).getType()) {
		case Up:
			iconName = "ic_up_folder";
			break;

		case Folder:
			iconName = "ic_folder";
			break;

		case File:
			iconName = "ic_file";
			break;

		default:
			iconName = "";
			break;
		}
		
		
		int id = resources.getIdentifier(iconName, "drawable",
				context.getPackageName());
		
		
		viewHolder.imageView.setImageResource(id);

		viewHolder.fileNameTextView.setText(webDavItemEntities.get(position).getName());
		
		return convertView;
	}

	private class ViewHolder {
		private ImageView imageView;
		private TextView fileNameTextView;

		private ViewHolder(ImageView imageView, TextView fileNameTextView) {
			this.imageView = imageView;
			this.fileNameTextView = fileNameTextView;
		}

	}

}
