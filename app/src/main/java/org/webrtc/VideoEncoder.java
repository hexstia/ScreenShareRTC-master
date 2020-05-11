//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import org.webrtc.EncodedImage.FrameType;

public interface VideoEncoder {
    VideoCodecStatus initEncode(VideoEncoder.Settings var1, VideoEncoder.Callback var2);

    VideoCodecStatus release();

    VideoCodecStatus encode(VideoFrame var1, VideoEncoder.EncodeInfo var2);

    VideoCodecStatus setChannelParameters(short var1, long var2);

    VideoCodecStatus setRateAllocation(VideoEncoder.BitrateAllocation var1, int var2);

    VideoEncoder.ScalingSettings getScalingSettings();

    String getImplementationName();

    public interface Callback {
        void onEncodedFrame(EncodedImage var1, VideoEncoder.CodecSpecificInfo var2);
    }

    public static class ScalingSettings {
        public final boolean on;
        public final int low;
        public final int high;

        public ScalingSettings(boolean on, int low, int high) {
            this.on = on;
            this.low = low;
            this.high = high;
        }
    }

    public static class BitrateAllocation {
        public final int[][] bitratesBbs;

        public BitrateAllocation(int[][] bitratesBbs) {
            this.bitratesBbs = bitratesBbs;
        }

        public int getSum() {
            int sum = 0;
            int[][] var2 = this.bitratesBbs;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                int[] spatialLayer = var2[var4];
                int[] var6 = spatialLayer;
                int var7 = spatialLayer.length;

                for(int var8 = 0; var8 < var7; ++var8) {
                    int bitrate = var6[var8];
                    sum += bitrate;
                }
            }

            return sum;
        }
    }

    public static class CodecSpecificInfoH264 extends VideoEncoder.CodecSpecificInfo {
        public CodecSpecificInfoH264() {
        }
    }

    public static class CodecSpecificInfoVP9 extends VideoEncoder.CodecSpecificInfo {
        public CodecSpecificInfoVP9() {
        }
    }

    public static class CodecSpecificInfoVP8 extends VideoEncoder.CodecSpecificInfo {
        public CodecSpecificInfoVP8() {
        }
    }

    public static class CodecSpecificInfo {
        public CodecSpecificInfo() {
        }
    }

    public static class EncodeInfo {
        public final FrameType[] frameTypes;

        public EncodeInfo(FrameType[] frameTypes) {
            this.frameTypes = frameTypes;
        }
    }

    public static class Settings {
        public final int numberOfCores;
        public final int width;
        public final int height;
        public final int startBitrate;
        public final int maxFramerate;

        public Settings(int numberOfCores, int width, int height, int startBitrate, int maxFramerate) {
            this.numberOfCores = numberOfCores;
            this.width = width;
            this.height = height;
            this.startBitrate = startBitrate;
            this.maxFramerate = maxFramerate;
        }
    }
}
