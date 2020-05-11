//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

public interface VideoEncoderFactory {
    VideoEncoder createEncoder(VideoCodecInfo var1);

    VideoCodecInfo[] getSupportedCodecs();
}
