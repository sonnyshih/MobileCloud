package com.sonnyshih.mobilecloud.upload;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.RequestEntity;

public class UploadRequestEntity implements RequestEntity{

	private final RequestEntity requestEntity;

    private final ProgressListener progressListener;
	
	public UploadRequestEntity(final RequestEntity entity,
			final ProgressListener progressListener) {
		super();
		this.requestEntity = entity;
		this.progressListener = progressListener;
	}
	
	@Override
	public long getContentLength() {
		return requestEntity.getContentLength();
	}

	@Override
	public String getContentType() {
		return requestEntity.getContentType();
	}

	@Override
	public boolean isRepeatable() {
		return requestEntity.isRepeatable();
	}

	@Override
	public void writeRequest(final OutputStream out) throws IOException {
		this.requestEntity.writeRequest(new CountingOutputStream(out, progressListener));
		
	}

	
	public static class CountingOutputStream extends FilterOutputStream {

        private final ProgressListener listener;

        private long transferred;

        public CountingOutputStream(final OutputStream out,
                final ProgressListener listener) {
            super(out);
            this.listener = listener;
            this.transferred = 0;
        }

        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            this.transferred += len;
            this.listener.transferred(this.transferred);
        }

        public void write(int b) throws IOException {
            out.write(b);
            this.transferred++;
            this.listener.transferred(this.transferred);
        }
    }	
	
    public static interface ProgressListener {
        void transferred(long num);
    }
}
