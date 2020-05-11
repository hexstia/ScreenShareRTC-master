//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

class BaseBitrateAdjuster implements BitrateAdjuster {
    protected int targetBitrateBps = 0;
    protected int targetFps = 0;

    BaseBitrateAdjuster() {
    }

    public void setTargets(int targetBitrateBps, int targetFps) {
        this.targetBitrateBps = targetBitrateBps;
        this.targetFps = targetFps;
    }

    public void reportEncodedFrame(int size) {
    }

    public int getAdjustedBitrateBps() {
        return this.targetBitrateBps;
    }

    public int getAdjustedFramerate() {
        return this.targetFps;
    }
}
