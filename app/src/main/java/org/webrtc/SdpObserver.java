//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

public interface SdpObserver {
    void onCreateSuccess(SessionDescription var1);

    void onSetSuccess();

    void onCreateFailure(String var1);

    void onSetFailure(String var1);
}
