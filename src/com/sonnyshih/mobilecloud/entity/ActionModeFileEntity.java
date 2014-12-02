package com.sonnyshih.mobilecloud.entity;

import java.io.File;
import java.io.Serializable;

public class ActionModeFileEntity implements Serializable{

	private static final long serialVersionUID = -3229990098346091099L;

	private File file;
	private boolean isChecked = false;
	
	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public boolean isChecked() {
		return isChecked;
	}
	
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	
	
}
