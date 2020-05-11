//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import org.webrtc.VideoFrame.I420Buffer;

import java.nio.ByteBuffer;

class WrappedNativeI420Buffer implements I420Buffer {
    private final int width;
    private final int height;
    private final ByteBuffer dataY;
    private final int strideY;
    private final ByteBuffer dataU;
    private final int strideU;
    private final ByteBuffer dataV;
    private final int strideV;
    private final long nativeBuffer;

    WrappedNativeI420Buffer(int width, int height, ByteBuffer dataY, int strideY, ByteBuffer dataU, int strideU, ByteBuffer dataV, int strideV, long nativeBuffer) {
        this.width = width;
        this.height = height;
        this.dataY = dataY;
        this.strideY = strideY;
        this.dataU = dataU;
        this.strideU = strideU;
        this.dataV = dataV;
        this.strideV = strideV;
        this.nativeBuffer = nativeBuffer;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public ByteBuffer getDataY() {
        return this.dataY;
    }

    public ByteBuffer getDataU() {
        return this.dataU;
    }

    public ByteBuffer getDataV() {
        return this.dataV;
    }

    public int getStrideY() {
        return this.strideY;
    }

    public int getStrideU() {
        return this.strideU;
    }

    public int getStrideV() {
        return this.strideV;
    }

    public I420Buffer toI420() {
        return this;
    }

    public void retain() {
        nativeAddRef(this.nativeBuffer);
    }

    public void release() {
        nativeRelease(this.nativeBuffer);
    }

    private static native long nativeAddRef(long var0);

    private static native long nativeRelease(long var0);
}
