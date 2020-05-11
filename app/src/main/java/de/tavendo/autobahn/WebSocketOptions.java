//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.tavendo.autobahn;

public class WebSocketOptions {
    private int mMaxFramePayloadSize;
    private int mMaxMessagePayloadSize;
    private boolean mReceiveTextMessagesRaw;
    private boolean mTcpNoDelay;
    private int mSocketReceiveTimeout;
    private int mSocketConnectTimeout;
    private boolean mValidateIncomingUtf8;
    private boolean mMaskClientFrames;
    private int mReconnectInterval;

    public WebSocketOptions() {
        this.mMaxFramePayloadSize = 131072;
        this.mMaxMessagePayloadSize = 131072;
        this.mReceiveTextMessagesRaw = false;
        this.mTcpNoDelay = true;
        this.mSocketReceiveTimeout = 200;
        this.mSocketConnectTimeout = 6000;
        this.mValidateIncomingUtf8 = true;
        this.mMaskClientFrames = true;
        this.mReconnectInterval = 0;
    }

    public WebSocketOptions(WebSocketOptions other) {
        this.mMaxFramePayloadSize = other.mMaxFramePayloadSize;
        this.mMaxMessagePayloadSize = other.mMaxMessagePayloadSize;
        this.mReceiveTextMessagesRaw = other.mReceiveTextMessagesRaw;
        this.mTcpNoDelay = other.mTcpNoDelay;
        this.mSocketReceiveTimeout = other.mSocketReceiveTimeout;
        this.mSocketConnectTimeout = other.mSocketConnectTimeout;
        this.mValidateIncomingUtf8 = other.mValidateIncomingUtf8;
        this.mMaskClientFrames = other.mMaskClientFrames;
        this.mReconnectInterval = other.mReconnectInterval;
    }

    public void setReceiveTextMessagesRaw(boolean enabled) {
        this.mReceiveTextMessagesRaw = enabled;
    }

    public boolean getReceiveTextMessagesRaw() {
        return this.mReceiveTextMessagesRaw;
    }

    public void setMaxFramePayloadSize(int size) {
        if (size > 0) {
            this.mMaxFramePayloadSize = size;
            if (this.mMaxMessagePayloadSize < this.mMaxFramePayloadSize) {
                this.mMaxMessagePayloadSize = this.mMaxFramePayloadSize;
            }
        }

    }

    public int getMaxFramePayloadSize() {
        return this.mMaxFramePayloadSize;
    }

    public void setMaxMessagePayloadSize(int size) {
        if (size > 0) {
            this.mMaxMessagePayloadSize = size;
            if (this.mMaxMessagePayloadSize < this.mMaxFramePayloadSize) {
                this.mMaxFramePayloadSize = this.mMaxMessagePayloadSize;
            }
        }

    }

    public int getMaxMessagePayloadSize() {
        return this.mMaxMessagePayloadSize;
    }

    public void setTcpNoDelay(boolean enabled) {
        this.mTcpNoDelay = enabled;
    }

    public boolean getTcpNoDelay() {
        return this.mTcpNoDelay;
    }

    public void setSocketReceiveTimeout(int timeoutMs) {
        if (timeoutMs >= 0) {
            this.mSocketReceiveTimeout = timeoutMs;
        }

    }

    public int getSocketReceiveTimeout() {
        return this.mSocketReceiveTimeout;
    }

    public void setSocketConnectTimeout(int timeoutMs) {
        if (timeoutMs >= 0) {
            this.mSocketConnectTimeout = timeoutMs;
        }

    }

    public int getSocketConnectTimeout() {
        return this.mSocketConnectTimeout;
    }

    public void setValidateIncomingUtf8(boolean enabled) {
        this.mValidateIncomingUtf8 = enabled;
    }

    public boolean getValidateIncomingUtf8() {
        return this.mValidateIncomingUtf8;
    }

    public void setMaskClientFrames(boolean enabled) {
        this.mMaskClientFrames = enabled;
    }

    public boolean getMaskClientFrames() {
        return this.mMaskClientFrames;
    }

    public void setReconnectInterval(int reconnectInterval) {
        this.mReconnectInterval = reconnectInterval;
    }

    public int getReconnectInterval() {
        return this.mReconnectInterval;
    }
}
