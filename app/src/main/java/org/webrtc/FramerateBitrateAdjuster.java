//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

class FramerateBitrateAdjuster extends BaseBitrateAdjuster {
    private static final int INITIAL_FPS = 30;

    FramerateBitrateAdjuster() {
    }

    public void setTargets(int targetBitrateBps, int targetFps) {
        if (this.targetFps == 0) {
            targetFps = 30;
        }

        super.setTargets(targetBitrateBps, targetFps);
        this.targetBitrateBps *= 30 / this.targetFps;
    }
}
