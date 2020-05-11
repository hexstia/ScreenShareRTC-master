//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import org.webrtc.VideoDecoder.Callback;

class VideoDecoderWrapperCallback implements Callback {
    private final long nativeDecoder;

    public VideoDecoderWrapperCallback(long nativeDecoder) {
        this.nativeDecoder = nativeDecoder;
    }

    public void onDecodedFrame(VideoFrame frame, Integer decodeTimeMs, Integer qp) {
        nativeOnDecodedFrame(this.nativeDecoder, frame, decodeTimeMs, qp);
    }

    private static native void nativeOnDecodedFrame(long var0, VideoFrame var2, Integer var3, Integer var4);
}
