//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import org.webrtc.CameraEnumerationAndroid.CaptureFormat;
import org.webrtc.CameraVideoCapturer.CameraEventsHandler;

import java.util.List;

public interface CameraEnumerator {
    String[] getDeviceNames();

    boolean isFrontFacing(String var1);

    boolean isBackFacing(String var1);

    List<CaptureFormat> getSupportedFormats(String var1);

    CameraVideoCapturer createCapturer(String var1, CameraEventsHandler var2);
}
