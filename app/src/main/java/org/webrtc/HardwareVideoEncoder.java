//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.os.Bundle;
import android.view.Surface;

import org.webrtc.EncodedImage.Builder;
import org.webrtc.EncodedImage.FrameType;
import org.webrtc.VideoFrame.I420Buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

@TargetApi(19)
class HardwareVideoEncoder implements VideoEncoder {
    private static final String TAG = "HardwareVideoEncoder";
    private static final int VIDEO_ControlRateConstant = 2;
    private static final String KEY_BITRATE_MODE = "bitrate-mode";
    private static final int MAX_VIDEO_FRAMERATE = 30;
    private static final int MAX_ENCODER_Q_SIZE = 2;
    private static final int MEDIA_CODEC_RELEASE_TIMEOUT_MS = 5000;
    private static final int DEQUEUE_OUTPUT_BUFFER_TIMEOUT_US = 100000;
    private final String codecName;
    private final VideoCodecType codecType;
    private final int colorFormat;
    private final HardwareVideoEncoder.ColorFormat inputColorFormat;
    private final int keyFrameIntervalSec;
    private final long forcedKeyFrameMs;
    private long lastKeyFrameMs;
    private final BitrateAdjuster bitrateAdjuster;
    private int adjustedBitrate;
    private final Deque<Builder> outputBuilders;
    private Thread outputThread;
    private volatile boolean running = false;
    private volatile Exception shutdownException = null;
    private MediaCodec codec;
    private Callback callback;
    private int width;
    private int height;
    private ByteBuffer configBuffer = null;

    public HardwareVideoEncoder(String codecName, VideoCodecType codecType, int colorFormat, int keyFrameIntervalSec, int forceKeyFrameIntervalMs, BitrateAdjuster bitrateAdjuster) {
        this.codecName = codecName;
        this.codecType = codecType;
        this.colorFormat = colorFormat;
        this.inputColorFormat = HardwareVideoEncoder.ColorFormat.valueOf(colorFormat);
        this.keyFrameIntervalSec = keyFrameIntervalSec;
        this.forcedKeyFrameMs = (long)forceKeyFrameIntervalMs;
        this.bitrateAdjuster = bitrateAdjuster;
        this.outputBuilders = new LinkedBlockingDeque();
    }

    public VideoCodecStatus initEncode(Settings settings, Callback callback) {
        return this.initEncodeInternal(settings.width, settings.height, settings.startBitrate, settings.maxFramerate, callback);
    }

    private VideoCodecStatus initEncodeInternal(int width, int height, int bitrateKbps, int fps, Callback callback) {
        Logging.d("HardwareVideoEncoder", "initEncode: " + width + " x " + height + ". @ " + bitrateKbps + "kbps. Fps: " + fps);
        this.width = width;
        this.height = height;
        if (bitrateKbps != 0 && fps != 0) {
            this.bitrateAdjuster.setTargets(bitrateKbps * 1000, fps);
        }

        this.adjustedBitrate = this.bitrateAdjuster.getAdjustedBitrateBps();
        this.callback = callback;
        this.lastKeyFrameMs = -1L;

        try {
            this.codec = MediaCodec.createByCodecName(this.codecName);
        } catch (IllegalArgumentException | IOException var8) {
            Logging.e("HardwareVideoEncoder", "Cannot create media encoder " + this.codecName);
            return VideoCodecStatus.ERROR;
        }

        try {
            MediaFormat format = MediaFormat.createVideoFormat(this.codecType.mimeType(), width, height);
            format.setInteger("bitrate", this.adjustedBitrate);
            format.setInteger("bitrate-mode", 2);
            format.setInteger("color-format", this.colorFormat);
            format.setInteger("frame-rate", this.bitrateAdjuster.getAdjustedFramerate());
            format.setInteger("i-frame-interval", this.keyFrameIntervalSec);
            Logging.d("HardwareVideoEncoder", "Format: " + format);
            this.codec.configure(format, (Surface)null, (MediaCrypto)null, 1);
            this.codec.start();
        } catch (IllegalStateException var7) {
            Logging.e("HardwareVideoEncoder", "initEncode failed", var7);
            this.release();
            return VideoCodecStatus.ERROR;
        }

        this.running = true;
        this.outputThread = this.createOutputThread();
        this.outputThread.start();
        return VideoCodecStatus.OK;
    }

    public VideoCodecStatus release() {
        try {
            this.running = false;
            VideoCodecStatus var1;
            if (!ThreadUtils.joinUninterruptibly(this.outputThread, 5000L)) {
                Logging.e("HardwareVideoEncoder", "Media encoder release timeout");
                var1 = VideoCodecStatus.TIMEOUT;
                return var1;
            }

            if (this.shutdownException != null) {
                Logging.e("HardwareVideoEncoder", "Media encoder release exception", this.shutdownException);
                var1 = VideoCodecStatus.ERROR;
                return var1;
            }
        } finally {
            this.codec = null;
            this.outputThread = null;
            this.outputBuilders.clear();
        }

        return VideoCodecStatus.OK;
    }

    public VideoCodecStatus encode(VideoFrame videoFrame, EncodeInfo encodeInfo) {
        if (this.codec == null) {
            return VideoCodecStatus.UNINITIALIZED;
        } else {
            int frameWidth = videoFrame.getWidth();
            int frameHeight = videoFrame.getHeight();
            if (frameWidth != this.width || frameHeight != this.height) {
                VideoCodecStatus status = this.resetCodec(frameWidth, frameHeight);
                if (status != VideoCodecStatus.OK) {
                    return status;
                }
            }

            int index;
            try {
                index = this.codec.dequeueInputBuffer(0L);
            } catch (IllegalStateException var18) {
                Logging.e("HardwareVideoEncoder", "dequeueInputBuffer failed", var18);
                return VideoCodecStatus.FALLBACK_SOFTWARE;
            }

            if (index == -1) {
                Logging.e("HardwareVideoEncoder", "Dropped frame, no input buffers available");
                return VideoCodecStatus.OK;
            } else if (this.outputBuilders.size() > 2) {
                Logging.e("HardwareVideoEncoder", "Dropped frame, encoder queue full");
                return VideoCodecStatus.OK;
            } else {
                ByteBuffer buffer;
                try {
                    buffer = this.codec.getInputBuffers()[index];
                } catch (IllegalStateException var17) {
                    Logging.e("HardwareVideoEncoder", "getInputBuffers failed", var17);
                    return VideoCodecStatus.FALLBACK_SOFTWARE;
                }

                I420Buffer i420 = videoFrame.getBuffer().toI420();
                this.inputColorFormat.fillBufferFromI420(buffer, i420);
                boolean requestedKeyFrame = false;
                FrameType[] var9 = encodeInfo.frameTypes;
                int var10 = var9.length;

                for(int var11 = 0; var11 < var10; ++var11) {
                    FrameType frameType = var9[var11];
                    if (frameType == FrameType.VideoFrameKey) {
                        requestedKeyFrame = true;
                    }
                }

                long presentationTimestampUs = (videoFrame.getTimestampNs() + 500L) / 1000L;
                long presentationTimestampMs = (presentationTimestampUs + 500L) / 1000L;
                if (requestedKeyFrame || this.shouldForceKeyFrame(presentationTimestampMs)) {
                    this.requestKeyFrame(presentationTimestampMs);
                }

                int bufferSize = videoFrame.getBuffer().getHeight() * videoFrame.getBuffer().getWidth() * 3 / 2;
                Builder builder = EncodedImage.builder().setCaptureTimeMs(presentationTimestampMs).setCompleteFrame(true).setEncodedWidth(videoFrame.getWidth()).setEncodedHeight(videoFrame.getHeight()).setRotation(videoFrame.getRotation());
                this.outputBuilders.offer(builder);

                try {
                    this.codec.queueInputBuffer(index, 0, bufferSize, presentationTimestampUs, 0);
                } catch (IllegalStateException var16) {
                    Logging.e("HardwareVideoEncoder", "queueInputBuffer failed", var16);
                    this.outputBuilders.pollLast();
                    return VideoCodecStatus.FALLBACK_SOFTWARE;
                }

                return VideoCodecStatus.OK;
            }
        }
    }

    public VideoCodecStatus setChannelParameters(short packetLoss, long roundTripTimeMs) {
        return VideoCodecStatus.OK;
    }

    public VideoCodecStatus setRateAllocation(BitrateAllocation bitrateAllocation, int framerate) {
        if (framerate > 30) {
            framerate = 30;
        }

        this.bitrateAdjuster.setTargets(bitrateAllocation.getSum(), framerate);
        return this.updateBitrate();
    }

    public ScalingSettings getScalingSettings() {
        return null;
    }

    public String getImplementationName() {
        return "HardwareVideoEncoder: " + this.codecName;
    }

    private VideoCodecStatus resetCodec(int newWidth, int newHeight) {
        VideoCodecStatus status = this.release();
        return status != VideoCodecStatus.OK ? status : this.initEncodeInternal(newWidth, newHeight, 0, 0, this.callback);
    }

    private boolean shouldForceKeyFrame(long presentationTimestampMs) {
        return this.forcedKeyFrameMs > 0L && presentationTimestampMs > this.lastKeyFrameMs + this.forcedKeyFrameMs;
    }

    private void requestKeyFrame(long presentationTimestampMs) {
        try {
            Bundle b = new Bundle();
            b.putInt("request-sync", 0);
            this.codec.setParameters(b);
        } catch (IllegalStateException var4) {
            Logging.e("HardwareVideoEncoder", "requestKeyFrame failed", var4);
            return;
        }

        this.lastKeyFrameMs = presentationTimestampMs;
    }

    private Thread createOutputThread() {
        return new Thread() {
            public void run() {
                while(HardwareVideoEncoder.this.running) {
                    HardwareVideoEncoder.this.deliverEncodedImage();
                }

                HardwareVideoEncoder.this.releaseCodecOnOutputThread();
            }
        };
    }

    private void deliverEncodedImage() {
        try {
            BufferInfo info = new BufferInfo();
            int index = this.codec.dequeueOutputBuffer(info, 100000L);
            if (index < 0) {
                return;
            }

            ByteBuffer codecOutputBuffer = this.codec.getOutputBuffers()[index];
            codecOutputBuffer.position(info.offset);
            codecOutputBuffer.limit(info.offset + info.size);
            if ((info.flags & 2) != 0) {
                Logging.d("HardwareVideoEncoder", "Config frame generated. Offset: " + info.offset + ". Size: " + info.size);
                this.configBuffer = ByteBuffer.allocateDirect(info.size);
                this.configBuffer.put(codecOutputBuffer);
            } else {
                this.bitrateAdjuster.reportEncodedFrame(info.size);
                if (this.adjustedBitrate != this.bitrateAdjuster.getAdjustedBitrateBps()) {
                    this.updateBitrate();
                }

                boolean isKeyFrame = (info.flags & 1) != 0;
                ByteBuffer frameBuffer;
                if (isKeyFrame && this.codecType == VideoCodecType.H264) {
                    Logging.d("HardwareVideoEncoder", "Prepending config frame of size " + this.configBuffer.capacity() + " to output buffer with offset " + info.offset + ", size " + info.size);
                    frameBuffer = ByteBuffer.allocateDirect(info.size + this.configBuffer.capacity());
                    this.configBuffer.rewind();
                    frameBuffer.put(this.configBuffer);
                } else {
                    frameBuffer = ByteBuffer.allocateDirect(info.size);
                }

                frameBuffer.put(codecOutputBuffer);
                frameBuffer.rewind();
                FrameType frameType = FrameType.VideoFrameDelta;
                if (isKeyFrame) {
                    Logging.d("HardwareVideoEncoder", "Sync frame generated");
                    frameType = FrameType.VideoFrameKey;
                }

                Builder builder = (Builder)this.outputBuilders.poll();
                builder.setBuffer(frameBuffer).setFrameType(frameType);
                this.callback.onEncodedFrame(builder.createEncodedImage(), new CodecSpecificInfo());
            }

            this.codec.releaseOutputBuffer(index, false);
        } catch (IllegalStateException var8) {
            Logging.e("HardwareVideoEncoder", "deliverOutput failed", var8);
        }

    }

    private void releaseCodecOnOutputThread() {
        Logging.d("HardwareVideoEncoder", "Releasing MediaCodec on output thread");

        try {
            this.codec.stop();
        } catch (Exception var3) {
            Logging.e("HardwareVideoEncoder", "Media encoder stop failed", var3);
        }

        try {
            this.codec.release();
        } catch (Exception var2) {
            Logging.e("HardwareVideoEncoder", "Media encoder release failed", var2);
            this.shutdownException = var2;
        }

        Logging.d("HardwareVideoEncoder", "Release on output thread done");
    }

    private VideoCodecStatus updateBitrate() {
        this.adjustedBitrate = this.bitrateAdjuster.getAdjustedBitrateBps();

        try {
            Bundle params = new Bundle();
            params.putInt("video-bitrate", this.adjustedBitrate);
            this.codec.setParameters(params);
            return VideoCodecStatus.OK;
        } catch (IllegalStateException var2) {
            Logging.e("HardwareVideoEncoder", "updateBitrate failed", var2);
            return VideoCodecStatus.ERROR;
        }
    }

    private static enum ColorFormat {
        I420 {
            void fillBufferFromI420(ByteBuffer buffer, I420Buffer i420) {
                buffer.put(i420.getDataY());
                buffer.put(i420.getDataU());
                buffer.put(i420.getDataV());
            }
        },
        NV12 {
            void fillBufferFromI420(ByteBuffer buffer, I420Buffer i420) {
                buffer.put(i420.getDataY());
                ByteBuffer u = i420.getDataU();
                ByteBuffer v = i420.getDataV();
                boolean var5 = false;

                while(u.hasRemaining() && v.hasRemaining()) {
                    buffer.put(u.get());
                    buffer.put(v.get());
                }

            }
        };

        private ColorFormat() {
        }

        abstract void fillBufferFromI420(ByteBuffer var1, I420Buffer var2);

        static HardwareVideoEncoder.ColorFormat valueOf(int colorFormat) {
            switch(colorFormat) {
                case 19:
                    return I420;
                case 21:
                case 2141391872:
                case 2141391876:
                    return NV12;
                default:
                    throw new IllegalArgumentException("Unsupported colorFormat: " + colorFormat);
            }
        }
    }
}
