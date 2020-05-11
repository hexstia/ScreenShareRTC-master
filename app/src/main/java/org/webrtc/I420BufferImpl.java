//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import org.webrtc.VideoFrame.I420Buffer;

import java.nio.ByteBuffer;

class I420BufferImpl implements I420Buffer {
    private final int width;
    private final int height;
    private final int strideUV;
    private final ByteBuffer y;
    private final ByteBuffer u;
    private final ByteBuffer v;

    I420BufferImpl(int width, int height) {
        this.width = width;
        this.height = height;
        this.strideUV = (width + 1) / 2;
        int halfHeight = (height + 1) / 2;
        this.y = ByteBuffer.allocateDirect(width * height);
        this.u = ByteBuffer.allocateDirect(this.strideUV * halfHeight);
        this.v = ByteBuffer.allocateDirect(this.strideUV * halfHeight);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public ByteBuffer getDataY() {
        return this.y;
    }

    public ByteBuffer getDataU() {
        return this.u;
    }

    public ByteBuffer getDataV() {
        return this.v;
    }

    public int getStrideY() {
        return this.width;
    }

    public int getStrideU() {
        return this.strideUV;
    }

    public int getStrideV() {
        return this.strideUV;
    }

    public I420Buffer toI420() {
        return this;
    }

    public void retain() {
    }

    public void release() {
    }
}
