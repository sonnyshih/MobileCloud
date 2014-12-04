package com.sonnyshih.mobilecloud.upload;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.RequestEntity;

public class UploadRequestEntity implements RequestEntity {

	/**
     * RequestEntity
     */
    private final RequestEntity entity;

    /**
     * ProgressListener
     */
    private final ProgressListener listener;

    /**
     * @param entity
     * @param listener
     */
    public UploadRequestEntity(final RequestEntity entity, final ProgressListener listener) {
            super();
            this.entity = entity;
            this.listener = listener;
    }

    /** (non-Javadoc)
     * @see org.apache.commons.httpclient.methods.RequestEntity#getContentLength()
     */
    public long getContentLength() {
            return this.entity.getContentLength();
    }

    /** (non-Javadoc)
     * @see org.apache.commons.httpclient.methods.RequestEntity#getContentType()
     */
    public String getContentType() {
            return this.entity.getContentType();
    }

    /** (non-Javadoc)
     * @see org.apache.commons.httpclient.methods.RequestEntity#isRepeatable()
     */
    public boolean isRepeatable() {
            return this.entity.isRepeatable();
    }

    /** (non-Javadoc)
     * @see org.apache.commons.httpclient.methods.RequestEntity#writeRequest(java.io.OutputStream)
     */
    public void writeRequest(final OutputStream out) throws IOException {
            this.entity.writeRequest(new CountingOutputStream(out, this.listener));
    }

    /**
     * @author Sa禳a Stamenkovi�?<umpirsky@gmail.com>
     *
     */
    public static interface ProgressListener {
            /**
             * @param bytes
             */
            void transferred(long bytes);
    }

    /**
     * @author <umpirsky@gmail.com>
     *
     */
    public static class CountingOutputStream extends FilterOutputStream {
            /**
             * ProgressListener
             */
            private final ProgressListener listener;
            /**
             * Bytes transfered
             */
            private long transferred;

            /**
             * @param out
             * @param listener
             */
            public CountingOutputStream(final OutputStream out, final ProgressListener listener) {
                    super(out);
                    this.listener = listener;
                    this.transferred = 0;
            }

            /** (non-Javadoc)
             * @see java.io.FilterOutputStream#write(byte[], int, int)
             */
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                    super.write(b, off, len);
                    this.transferred += len;
                    this.listener.transferred(this.transferred);
            }

            /** (non-Javadoc)
             * @see java.io.FilterOutputStream#write(int)
             */
            @Override
            public void write(int b) throws IOException {
                    super.write(b);
                    this.transferred++;
            }

    }

}
