//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

public interface VideoDecoder {
    VideoCodecStatus initDecode(VideoDecoder.Settings var1, VideoDecoder.Callback var2);

    VideoCodecStatus release();

    VideoCodecStatus decode(EncodedImage var1, VideoDecoder.DecodeInfo var2);

    boolean getPrefersLateDecoding();

    String getImplementationName();

    public interface Callback {
        void onDecodedFrame(VideoFrame var1, Integer var2, Integer var3);
    }

    public static class DecodeInfo {
        public final boolean isMissingFrames;
        public final long renderTimeMs;

        public DecodeInfo(boolean isMissingFrames, long renderTimeMs) {
            this.isMissingFrames = isMissingFrames;
            this.renderTimeMs = renderTimeMs;
        }
    }

    public static class Settings {
        public final int numberOfCores;
        public final int width;
        public final int height;

        public Settings(int numberOfCores, int width, int height) {
            this.numberOfCores = numberOfCores;
            this.width = width;
            this.height = height;
        }
    }
}
