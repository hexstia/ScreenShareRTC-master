//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import org.webrtc.MediaStreamTrack.MediaType;

import java.util.LinkedList;

public class RtpParameters {
    public final LinkedList<Encoding> encodings = new LinkedList();
    public final LinkedList<Codec> codecs = new LinkedList();

    public RtpParameters() {
    }

    public static class Codec {
        public int payloadType;
        public String name;
        MediaType kind;
        public Integer clockRate;
        public Integer numChannels;

        public Codec() {
        }
    }

    public static class Encoding {
        public boolean active = true;
        public Integer maxBitrateBps;
        public Long ssrc;

        public Encoding() {
        }
    }
}
