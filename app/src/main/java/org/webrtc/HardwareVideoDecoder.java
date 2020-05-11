//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import android.annotation.TargetApi;
import android.graphics.Matrix;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.os.SystemClock;
import android.view.Surface;

import org.webrtc.EncodedImage.FrameType;
import org.webrtc.ThreadUtils.ThreadChecker;
import org.webrtc.VideoFrame.Buffer;
import org.webrtc.VideoFrame.I420Buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

@TargetApi(16)
class HardwareVideoDecoder implements VideoDecoder {
    private static final String TAG = "HardwareVideoDecoder";
    private static final String MEDIA_FORMAT_KEY_STRIDE = "stride";
    private static final String MEDIA_FORMAT_KEY_SLICE_HEIGHT = "slice-height";
    private static final String MEDIA_FORMAT_KEY_CROP_LEFT = "crop-left";
    private static final String MEDIA_FORMAT_KEY_CROP_RIGHT = "crop-right";
    private static final String MEDIA_FORMAT_KEY_CROP_TOP = "crop-top";
    private static final String MEDIA_FORMAT_KEY_CROP_BOTTOM = "crop-bottom";
    private static final int MEDIA_CODEC_RELEASE_TIMEOUT_MS = 5000;
    private static final int DEQUEUE_INPUT_TIMEOUT_US = 500000;
    private static final int DEQUEUE_OUTPUT_BUFFER_TIMEOUT_US = 100000;
    private final String codecName;
    private final VideoCodecType codecType;
    private final Deque<FrameInfo> frameInfos;
    private int colorFormat;
    private Thread outputThread;
    private ThreadChecker outputThreadChecker;
    private ThreadChecker decoderThreadChecker;
    private volatile boolean running = false;
    private volatile Exception shutdownException = null;
    private final Object activeOutputBuffersLock = new Object();
    private int activeOutputBuffers = 0;
    private final Object dimensionLock = new Object();
    private int width;
    private int height;
    private int stride;
    private int sliceHeight;
    private boolean hasDecodedFirstFrame;
    private boolean keyFrameRequired;
    private Callback callback;
    private MediaCodec codec = null;

    HardwareVideoDecoder(String codecName, VideoCodecType codecType, int colorFormat) {
        if (!this.isSupportedColorFormat(colorFormat)) {
            throw new IllegalArgumentException("Unsupported color format: " + colorFormat);
        } else {
            this.codecName = codecName;
            this.codecType = codecType;
            this.colorFormat = colorFormat;
            this.frameInfos = new LinkedBlockingDeque();
        }
    }

    public VideoCodecStatus initDecode(Settings settings, Callback callback) {
        this.decoderThreadChecker = new ThreadChecker();
        return this.initDecodeInternal(settings.width, settings.height, callback);
    }

    private VideoCodecStatus initDecodeInternal(int width, int height, Callback callback) {
        this.decoderThreadChecker.checkIsOnValidThread();
        if (this.outputThread != null) {
            Logging.e("HardwareVideoDecoder", "initDecodeInternal called while the codec is already running");
            return VideoCodecStatus.ERROR;
        } else {
            this.callback = callback;
            this.width = width;
            this.height = height;
            this.stride = width;
            this.sliceHeight = height;
            this.hasDecodedFirstFrame = false;
            this.keyFrameRequired = true;

            try {
                this.codec = MediaCodec.createByCodecName(this.codecName);
            } catch (IllegalArgumentException | IOException var6) {
                Logging.e("HardwareVideoDecoder", "Cannot create media decoder " + this.codecName);
                return VideoCodecStatus.ERROR;
            }

            try {
                MediaFormat format = MediaFormat.createVideoFormat(this.codecType.mimeType(), width, height);
                format.setInteger("color-format", this.colorFormat);
                this.codec.configure(format, (Surface)null, (MediaCrypto)null, 0);
                this.codec.start();
            } catch (IllegalStateException var5) {
                Logging.e("HardwareVideoDecoder", "initDecode failed", var5);
                this.release();
                return VideoCodecStatus.ERROR;
            }

            this.running = true;
            this.outputThread = this.createOutputThread();
            this.outputThread.start();
            return VideoCodecStatus.OK;
        }
    }

    public VideoCodecStatus decode(EncodedImage frame, DecodeInfo info) {
        this.decoderThreadChecker.checkIsOnValidThread();
        if (this.codec != null && this.callback != null) {
            if (frame.buffer == null) {
                Logging.e("HardwareVideoDecoder", "decode() - no input data");
                return VideoCodecStatus.ERR_PARAMETER;
            } else {
                int size = frame.buffer.remaining();
                if (size == 0) {
                    Logging.e("HardwareVideoDecoder", "decode() - input buffer empty");
                    return VideoCodecStatus.ERR_PARAMETER;
                } else {
                    Object var6 = this.dimensionLock;
                    int width;
                    int height;
                    synchronized(this.dimensionLock) {
                        width = this.width;
                        height = this.height;
                    }

                    if (frame.encodedWidth * frame.encodedHeight > 0 && (frame.encodedWidth != width || frame.encodedHeight != height)) {
                        VideoCodecStatus status = this.reinitDecode(frame.encodedWidth, frame.encodedHeight);
                        if (status != VideoCodecStatus.OK) {
                            return status;
                        }
                    }

                    if (this.keyFrameRequired) {
                        if (frame.frameType != FrameType.VideoFrameKey) {
                            Logging.e("HardwareVideoDecoder", "decode() - key frame required first");
                            return VideoCodecStatus.ERROR;
                        }

                        if (!frame.completeFrame) {
                            Logging.e("HardwareVideoDecoder", "decode() - complete frame required first");
                            return VideoCodecStatus.ERROR;
                        }
                    }

                    int index;
                    try {
                        index = this.codec.dequeueInputBuffer(500000L);
                    } catch (IllegalStateException var11) {
                        Logging.e("HardwareVideoDecoder", "dequeueInputBuffer failed", var11);
                        return VideoCodecStatus.ERROR;
                    }

                    if (index < 0) {
                        Logging.e("HardwareVideoDecoder", "decode() - no HW buffers available; decoder falling behind");
                        return VideoCodecStatus.ERROR;
                    } else {
                        ByteBuffer buffer;
                        try {
                            buffer = this.codec.getInputBuffers()[index];
                        } catch (IllegalStateException var10) {
                            Logging.e("HardwareVideoDecoder", "getInputBuffers failed", var10);
                            return VideoCodecStatus.ERROR;
                        }

                        if (buffer.capacity() < size) {
                            Logging.e("HardwareVideoDecoder", "decode() - HW buffer too small");
                            return VideoCodecStatus.ERROR;
                        } else {
                            buffer.put(frame.buffer);
                            this.frameInfos.offer(new HardwareVideoDecoder.FrameInfo(SystemClock.elapsedRealtime(), frame.rotation));

                            try {
                                this.codec.queueInputBuffer(index, 0, size, frame.captureTimeMs * 1000L, 0);
                            } catch (IllegalStateException var9) {
                                Logging.e("HardwareVideoDecoder", "queueInputBuffer failed", var9);
                                this.frameInfos.pollLast();
                                return VideoCodecStatus.ERROR;
                            }

                            if (this.keyFrameRequired) {
                                this.keyFrameRequired = false;
                            }

                            return VideoCodecStatus.OK;
                        }
                    }
                }
            }
        } else {
            return VideoCodecStatus.UNINITIALIZED;
        }
    }

    public boolean getPrefersLateDecoding() {
        return true;
    }

    public String getImplementationName() {
        return "HardwareVideoDecoder: " + this.codecName;
    }

    public VideoCodecStatus release() {
        VideoCodecStatus var1;
        try {
            this.running = false;
            if (ThreadUtils.joinUninterruptibly(this.outputThread, 5000L)) {
                if (this.shutdownException == null) {
                    return VideoCodecStatus.OK;
                }

                Logging.e("HardwareVideoDecoder", "Media encoder release error", new RuntimeException(this.shutdownException));
                this.shutdownException = null;
                var1 = VideoCodecStatus.ERROR;
                return var1;
            }

            Logging.e("HardwareVideoDecoder", "Media encoder release timeout", new RuntimeException());
            var1 = VideoCodecStatus.TIMEOUT;
        } finally {
            this.codec = null;
            this.callback = null;
            this.outputThread = null;
            this.frameInfos.clear();
        }

        return var1;
    }

    private VideoCodecStatus reinitDecode(int newWidth, int newHeight) {
        this.decoderThreadChecker.checkIsOnValidThread();
        VideoCodecStatus status = this.release();
        return status != VideoCodecStatus.OK ? status : this.initDecodeInternal(newWidth, newHeight, this.callback);
    }

    private Thread createOutputThread() {
        return new Thread("HardwareVideoDecoder.outputThread") {
            public void run() {
                HardwareVideoDecoder.this.outputThreadChecker = new ThreadChecker();

                while(HardwareVideoDecoder.this.running) {
                    HardwareVideoDecoder.this.deliverDecodedFrame();
                }

                HardwareVideoDecoder.this.releaseCodecOnOutputThread();
            }
        };
    }

    private void deliverDecodedFrame() {
        this.outputThreadChecker.checkIsOnValidThread();

        try {
            BufferInfo info = new BufferInfo();
            int result = this.codec.dequeueOutputBuffer(info, 100000L);
            if (result == -2) {
                this.reformat(this.codec.getOutputFormat());
                return;
            }

            if (result < 0) {
                Logging.v("HardwareVideoDecoder", "dequeueOutputBuffer returned " + result);
                return;
            }

            HardwareVideoDecoder.FrameInfo frameInfo = (HardwareVideoDecoder.FrameInfo)this.frameInfos.poll();
            Integer decodeTimeMs = null;
            int rotation = 0;
            if (frameInfo != null) {
                decodeTimeMs = (int)(SystemClock.elapsedRealtime() - frameInfo.decodeStartTimeMs);
                rotation = frameInfo.rotation;
            }

            this.hasDecodedFirstFrame = true;
            Object var10 = this.dimensionLock;
            int width;
            int height;
            int stride;
            int sliceHeight;
            synchronized(this.dimensionLock) {
                width = this.width;
                height = this.height;
                stride = this.stride;
                sliceHeight = this.sliceHeight;
            }

            if (info.size < width * height * 3 / 2) {
                Logging.e("HardwareVideoDecoder", "Insufficient output buffer size: " + info.size);
                return;
            }

            if (info.size < stride * height * 3 / 2 && sliceHeight == height && stride > width) {
                stride = info.size * 2 / (height * 3);
            }

            ByteBuffer buffer = this.codec.getOutputBuffers()[result];
            buffer.position(info.offset);
            buffer.limit(info.size);
            Object frameBuffer;
            if (this.colorFormat == 19) {
                if (sliceHeight % 2 == 0) {
                    frameBuffer = this.createBufferFromI420(buffer, result, info.offset, stride, sliceHeight, width, height);
                } else {
                    frameBuffer = new I420BufferImpl(width, height);
                    copyI420(buffer, info.offset, (I420Buffer)frameBuffer, stride, sliceHeight, width, height);
                    this.codec.releaseOutputBuffer(result, false);
                }
            } else {
                frameBuffer = new I420BufferImpl(width, height);
                nv12ToI420(buffer, info.offset, (I420Buffer)frameBuffer, stride, sliceHeight, width, height);
                this.codec.releaseOutputBuffer(result, false);
            }

            long presentationTimeNs = info.presentationTimeUs * 1000L;
            VideoFrame frame = new VideoFrame((Buffer)frameBuffer, rotation, presentationTimeNs, new Matrix());
            this.callback.onDecodedFrame(frame, decodeTimeMs, (Integer)null);
            frame.release();
        } catch (IllegalStateException var16) {
            Logging.e("HardwareVideoDecoder", "deliverDecodedFrame failed", var16);
        }

    }

    private void reformat(MediaFormat format) {
        this.outputThreadChecker.checkIsOnValidThread();
        Logging.d("HardwareVideoDecoder", "Decoder format changed: " + format.toString());
        int newWidth;
        int newHeight;
        if (format.containsKey("crop-left") && format.containsKey("crop-right") && format.containsKey("crop-bottom") && format.containsKey("crop-top")) {
            newWidth = 1 + format.getInteger("crop-right") - format.getInteger("crop-left");
            newHeight = 1 + format.getInteger("crop-bottom") - format.getInteger("crop-top");
        } else {
            newWidth = format.getInteger("width");
            newHeight = format.getInteger("height");
        }

        Object var4 = this.dimensionLock;
        synchronized(this.dimensionLock) {
            if (this.hasDecodedFirstFrame && (this.width != newWidth || this.height != newHeight)) {
                this.stopOnOutputThread(new RuntimeException("Unexpected size change. Configured " + this.width + "*" + this.height + ". New " + newWidth + "*" + newHeight));
                return;
            }

            this.width = newWidth;
            this.height = newHeight;
        }

        if (format.containsKey("color-format")) {
            this.colorFormat = format.getInteger("color-format");
            Logging.d("HardwareVideoDecoder", "Color: 0x" + Integer.toHexString(this.colorFormat));
            if (!this.isSupportedColorFormat(this.colorFormat)) {
                this.stopOnOutputThread(new IllegalStateException("Unsupported color format: " + this.colorFormat));
                return;
            }
        }

        var4 = this.dimensionLock;
        synchronized(this.dimensionLock) {
            if (format.containsKey("stride")) {
                this.stride = format.getInteger("stride");
            }

            if (format.containsKey("slice-height")) {
                this.sliceHeight = format.getInteger("slice-height");
            }

            Logging.d("HardwareVideoDecoder", "Frame stride and slice height: " + this.stride + " x " + this.sliceHeight);
            this.stride = Math.max(this.width, this.stride);
            this.sliceHeight = Math.max(this.height, this.sliceHeight);
        }
    }

    private void releaseCodecOnOutputThread() {
        this.outputThreadChecker.checkIsOnValidThread();
        Logging.d("HardwareVideoDecoder", "Releasing MediaCodec on output thread");
        this.waitOutputBuffersReleasedOnOutputThread();

        try {
            this.codec.stop();
        } catch (Exception var3) {
            Logging.e("HardwareVideoDecoder", "Media decoder stop failed", var3);
        }

        try {
            this.codec.release();
        } catch (Exception var2) {
            Logging.e("HardwareVideoDecoder", "Media decoder release failed", var2);
            this.shutdownException = var2;
        }

        this.codec = null;
        this.callback = null;
        this.outputThread = null;
        this.frameInfos.clear();
        Logging.d("HardwareVideoDecoder", "Release on output thread done");
    }

    private void waitOutputBuffersReleasedOnOutputThread() {
        this.outputThreadChecker.checkIsOnValidThread();
        Object var1 = this.activeOutputBuffersLock;
        synchronized(this.activeOutputBuffersLock) {
            while(this.activeOutputBuffers > 0) {
                Logging.d("HardwareVideoDecoder", "Waiting for all frames to be released.");

                try {
                    this.activeOutputBuffersLock.wait();
                } catch (InterruptedException var4) {
                    Logging.e("HardwareVideoDecoder", "Interrupted while waiting for output buffers to be released.", var4);
                    return;
                }
            }

        }
    }

    private void stopOnOutputThread(Exception e) {
        this.outputThreadChecker.checkIsOnValidThread();
        this.running = false;
        this.shutdownException = e;
    }

    private boolean isSupportedColorFormat(int colorFormat) {
        int[] var2 = MediaCodecUtils.DECODER_COLOR_FORMATS;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            int supported = var2[var4];
            if (supported == colorFormat) {
                return true;
            }
        }

        return false;
    }

    private I420Buffer createBufferFromI420(final ByteBuffer buffer, final int outputBufferIndex, final int offset, final int stride, int sliceHeight, final int width, final int height) {
        final int uvStride = stride / 2;
        int chromaWidth = (width + 1) / 2;
        final int chromaHeight = (height + 1) / 2;
        final int uPos = offset + stride * sliceHeight;
        final int vPos = uPos + uvStride * sliceHeight / 2;
        Object var14 = this.activeOutputBuffersLock;
        synchronized(this.activeOutputBuffersLock) {
            ++this.activeOutputBuffers;
        }

        return new I420Buffer() {
            private int refCount = 1;

            public ByteBuffer getDataY() {
                ByteBuffer data = buffer.slice();
                data.position(offset);
                data.limit(offset + this.getStrideY() * height);
                return data;
            }

            public ByteBuffer getDataU() {
                ByteBuffer data = buffer.slice();
                data.position(uPos);
                data.limit(uPos + this.getStrideU() * chromaHeight);
                return data;
            }

            public ByteBuffer getDataV() {
                ByteBuffer data = buffer.slice();
                data.position(vPos);
                data.limit(vPos + this.getStrideV() * chromaHeight);
                return data;
            }

            public int getStrideY() {
                return stride;
            }

            public int getStrideU() {
                return uvStride;
            }

            public int getStrideV() {
                return uvStride;
            }

            public int getWidth() {
                return width;
            }

            public int getHeight() {
                return height;
            }

            public I420Buffer toI420() {
                return this;
            }

            public void retain() {
                ++this.refCount;
            }

            public void release() {
                --this.refCount;
                if (this.refCount == 0) {
                    HardwareVideoDecoder.this.codec.releaseOutputBuffer(outputBufferIndex, false);
                    synchronized(HardwareVideoDecoder.this.activeOutputBuffersLock) {
                        HardwareVideoDecoder.this.activeOutputBuffers--;
                        HardwareVideoDecoder.this.activeOutputBuffersLock.notifyAll();
                    }
                }

            }
        };
    }

    private static void copyI420(ByteBuffer src, int offset, I420Buffer frameBuffer, int stride, int sliceHeight, int width, int height) {
        int uvStride = stride / 2;
        int chromaWidth = (width + 1) / 2;
        int chromaHeight = sliceHeight % 2 == 0 ? (height + 1) / 2 : height / 2;
        int uPos = offset + stride * sliceHeight;
        int vPos = uPos + uvStride * sliceHeight / 2;
        copyPlane(src, offset, stride, frameBuffer.getDataY(), 0, frameBuffer.getStrideY(), width, height);
        copyPlane(src, uPos, uvStride, frameBuffer.getDataU(), 0, frameBuffer.getStrideU(), chromaWidth, chromaHeight);
        copyPlane(src, vPos, uvStride, frameBuffer.getDataV(), 0, frameBuffer.getStrideV(), chromaWidth, chromaHeight);
        if (sliceHeight % 2 != 0) {
            int strideU = frameBuffer.getStrideU();
            int endU = chromaHeight * strideU;
            copyRow(frameBuffer.getDataU(), endU - strideU, frameBuffer.getDataU(), endU, chromaWidth);
            int strideV = frameBuffer.getStrideV();
            int endV = chromaHeight * strideV;
            copyRow(frameBuffer.getDataV(), endV - strideV, frameBuffer.getDataV(), endV, chromaWidth);
        }

    }

    private static void nv12ToI420(ByteBuffer src, int offset, I420Buffer frameBuffer, int stride, int sliceHeight, int width, int height) {
        int uvPos = offset + stride * sliceHeight;
        int chromaWidth = (width + 1) / 2;
        int chromaHeight = (height + 1) / 2;
        copyPlane(src, offset, stride, frameBuffer.getDataY(), 0, frameBuffer.getStrideY(), width, height);
        int dstUPos = 0;
        int dstVPos = 0;

        for(int i = 0; i < chromaHeight; ++i) {
            for(int j = 0; j < chromaWidth; ++j) {
                frameBuffer.getDataU().put(dstUPos + j, src.get(uvPos + j * 2));
                frameBuffer.getDataV().put(dstVPos + j, src.get(uvPos + j * 2 + 1));
            }

            dstUPos += frameBuffer.getStrideU();
            dstVPos += frameBuffer.getStrideV();
            uvPos += stride;
        }

    }

    private static void copyPlane(ByteBuffer src, int srcPos, int srcStride, ByteBuffer dst, int dstPos, int dstStride, int width, int height) {
        for(int i = 0; i < height; ++i) {
            copyRow(src, srcPos, dst, dstPos, width);
            srcPos += srcStride;
            dstPos += dstStride;
        }

    }

    private static void copyRow(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int width) {
        for(int i = 0; i < width; ++i) {
            dst.put(dstPos + i, src.get(srcPos + i));
        }

    }

    private static class FrameInfo {
        final long decodeStartTimeMs;
        final int rotation;

        FrameInfo(long decodeStartTimeMs, int rotation) {
            this.decodeStartTimeMs = decodeStartTimeMs;
            this.rotation = rotation;
        }
    }
}
