//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import java.nio.ByteBuffer;

public class EncodedImage {
    public final ByteBuffer buffer;
    public final int encodedWidth;
    public final int encodedHeight;
    public final long captureTimeMs;
    public final EncodedImage.FrameType frameType;
    public final int rotation;
    public final boolean completeFrame;
    public final Integer qp;

    private EncodedImage(ByteBuffer buffer, int encodedWidth, int encodedHeight, long captureTimeMs, EncodedImage.FrameType frameType, int rotation, boolean completeFrame, Integer qp) {
        this.buffer = buffer;
        this.encodedWidth = encodedWidth;
        this.encodedHeight = encodedHeight;
        this.captureTimeMs = captureTimeMs;
        this.frameType = frameType;
        this.rotation = rotation;
        this.completeFrame = completeFrame;
        this.qp = qp;
    }

    public static EncodedImage.Builder builder() {
        return new EncodedImage.Builder();
    }

    public static class Builder {
        private ByteBuffer buffer;
        private int encodedWidth;
        private int encodedHeight;
        private long captureTimeMs;
        private EncodedImage.FrameType frameType;
        private int rotation;
        private boolean completeFrame;
        private Integer qp;

        private Builder() {
        }

        public EncodedImage.Builder setBuffer(ByteBuffer buffer) {
            this.buffer = buffer;
            return this;
        }

        public EncodedImage.Builder setEncodedWidth(int encodedWidth) {
            this.encodedWidth = encodedWidth;
            return this;
        }

        public EncodedImage.Builder setEncodedHeight(int encodedHeight) {
            this.encodedHeight = encodedHeight;
            return this;
        }

        public EncodedImage.Builder setCaptureTimeMs(long captureTimeMs) {
            this.captureTimeMs = captureTimeMs;
            return this;
        }

        public EncodedImage.Builder setFrameType(EncodedImage.FrameType frameType) {
            this.frameType = frameType;
            return this;
        }

        public EncodedImage.Builder setRotation(int rotation) {
            this.rotation = rotation;
            return this;
        }

        public EncodedImage.Builder setCompleteFrame(boolean completeFrame) {
            this.completeFrame = completeFrame;
            return this;
        }

        public EncodedImage.Builder setQp(Integer qp) {
            this.qp = qp;
            return this;
        }

        public EncodedImage createEncodedImage() {
            return new EncodedImage(this.buffer, this.encodedWidth, this.encodedHeight, this.captureTimeMs, this.frameType, this.rotation, this.completeFrame, this.qp);
        }
    }

    public static enum FrameType {
        EmptyFrame,
        VideoFrameKey,
        VideoFrameDelta;

        private FrameType() {
        }
    }
}
