//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

interface BitrateAdjuster {
    void setTargets(int var1, int var2);

    void reportEncodedFrame(int var1);

    int getAdjustedBitrateBps();

    int getAdjustedFramerate();
}
