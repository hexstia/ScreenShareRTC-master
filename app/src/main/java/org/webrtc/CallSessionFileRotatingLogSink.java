//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import org.webrtc.Logging.Severity;

public class CallSessionFileRotatingLogSink {
    private long nativeSink;

    public static byte[] getLogData(String dirPath) {
        return nativeGetLogData(dirPath);
    }

    public CallSessionFileRotatingLogSink(String dirPath, int maxFileSize, Severity severity) {
        this.nativeSink = nativeAddSink(dirPath, maxFileSize, severity.ordinal());
    }

    public void dispose() {
        if (this.nativeSink != 0L) {
            nativeDeleteSink(this.nativeSink);
            this.nativeSink = 0L;
        }

    }

    private static native long nativeAddSink(String var0, int var1, int var2);

    private static native void nativeDeleteSink(long var0);

    private static native byte[] nativeGetLogData(String var0);

    static {
        System.loadLibrary("jingle_peerconnection_so");
    }
}
