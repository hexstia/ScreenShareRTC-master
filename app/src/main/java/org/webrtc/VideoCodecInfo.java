//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import java.util.Map;

public class VideoCodecInfo {
    public final int payload;
    public final String name;
    public final Map<String, String> params;

    public VideoCodecInfo(int payload, String name, Map<String, String> params) {
        this.payload = payload;
        this.name = name;
        this.params = params;
    }
}
