//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import android.graphics.Matrix;

import java.nio.ByteBuffer;

public class VideoFrame {
    private final VideoFrame.Buffer buffer;
    private final int rotation;
    private final long timestampNs;
    private final Matrix transformMatrix;

    public VideoFrame(VideoFrame.Buffer buffer, int rotation, long timestampNs, Matrix transformMatrix) {
        if (buffer == null) {
            throw new IllegalArgumentException("buffer not allowed to be null");
        } else if (transformMatrix == null) {
            throw new IllegalArgumentException("transformMatrix not allowed to be null");
        } else {
            this.buffer = buffer;
            this.rotation = rotation;
            this.timestampNs = timestampNs;
            this.transformMatrix = transformMatrix;
        }
    }

    public VideoFrame.Buffer getBuffer() {
        return this.buffer;
    }

    public int getRotation() {
        return this.rotation;
    }

    public long getTimestampNs() {
        return this.timestampNs;
    }

    public Matrix getTransformMatrix() {
        return this.transformMatrix;
    }

    public int getWidth() {
        return this.buffer.getWidth();
    }

    public int getHeight() {
        return this.buffer.getHeight();
    }

    public void retain() {
        this.buffer.retain();
    }

    public void release() {
        this.buffer.release();
    }

    public interface TextureBuffer extends VideoFrame.Buffer {
        VideoFrame.TextureBuffer.Type getType();

        int getTextureId();

        public static enum Type {
            OES,
            RGB;

            private Type() {
            }
        }
    }

    public interface I420Buffer extends VideoFrame.Buffer {
        ByteBuffer getDataY();

        ByteBuffer getDataU();

        ByteBuffer getDataV();

        int getStrideY();

        int getStrideU();

        int getStrideV();
    }

    public interface Buffer {
        int getWidth();

        int getHeight();

        VideoFrame.I420Buffer toI420();

        void retain();

        void release();
    }
}
