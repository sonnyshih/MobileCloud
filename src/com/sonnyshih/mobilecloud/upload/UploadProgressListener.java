package com.sonnyshih.mobilecloud.upload;

import java.util.Observable;
import java.util.Observer;

import com.sonnyshih.mobilecloud.upload.UploadRequestEntity.ProgressListener;


public class UploadProgressListener extends Observable implements
		ProgressListener {

	/**
     * Number of bytes to send
     */
    private long bytesToSend;

    /**
     * Progress constructor
     *
     * @param observer applet is observer. monitors upload progress
     * @param bytesToSend number of bytes that need to be sent
     */
    public UploadProgressListener(Observer observer, long bytesToSend) {
            // for some reson, twice much bytes is sent to server, so this is ugly patch
            this.bytesToSend = 2 * bytesToSend;
            this.addObserver(observer);
    }

    /**
     * Triggered on every byte sent
     *
     * @param bytes number of bytes sent
     */
    public void transferred(long bytes) {
            this.setChanged();
            this.notifyObservers((float) bytes/this.bytesToSend * 50);
    }

}
