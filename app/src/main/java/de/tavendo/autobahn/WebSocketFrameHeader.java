//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.tavendo.autobahn;

public class WebSocketFrameHeader {
    private int mOpcode;
    private boolean mFin;
    private int mReserved;
    private int mHeaderLen;
    private int mPayloadLen;
    private int mTotalLen;
    private byte[] mMask;

    public WebSocketFrameHeader() {
    }

    public int getOpcode() {
        return this.mOpcode;
    }

    public void setOpcode(int opcode) {
        this.mOpcode = opcode;
    }

    public boolean isFin() {
        return this.mFin;
    }

    public void setFin(boolean fin) {
        this.mFin = fin;
    }

    public int getReserved() {
        return this.mReserved;
    }

    public void setReserved(int reserved) {
        this.mReserved = reserved;
    }

    public int getHeaderLength() {
        return this.mHeaderLen;
    }

    public void setHeaderLength(int headerLength) {
        this.mHeaderLen = headerLength;
    }

    public int getPayloadLength() {
        return this.mPayloadLen;
    }

    public void setPayloadLength(int payloadLength) {
        this.mPayloadLen = payloadLength;
    }

    public int getTotalLength() {
        return this.mTotalLen;
    }

    public void setTotalLen(int totalLength) {
        this.mTotalLen = totalLength;
    }

    public byte[] getMask() {
        return this.mMask;
    }

    public void setMask(byte[] mask) {
        this.mMask = mask;
    }
}
