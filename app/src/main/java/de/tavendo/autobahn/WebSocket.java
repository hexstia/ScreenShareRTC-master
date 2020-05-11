//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.tavendo.autobahn;

import java.net.URI;

public interface WebSocket {
    String UTF8_ENCODING = "UTF-8";

    void connect(URI var1, WebSocket.WebSocketConnectionObserver var2) throws WebSocketException;

    void connect(URI var1, WebSocket.WebSocketConnectionObserver var2, WebSocketOptions var3) throws WebSocketException;

    void disconnect();

    boolean isConnected();

    void sendBinaryMessage(byte[] var1);

    void sendRawTextMessage(byte[] var1);

    void sendTextMessage(String var1);

    public interface WebSocketConnectionObserver {
        void onOpen();

        void onClose(WebSocket.WebSocketConnectionObserver.WebSocketCloseNotification var1, String var2);

        void onTextMessage(String var1);

        void onRawTextMessage(byte[] var1);

        void onBinaryMessage(byte[] var1);

        public static enum WebSocketCloseNotification {
            NORMAL,
            CANNOT_CONNECT,
            CONNECTION_LOST,
            PROTOCOL_ERROR,
            INTERNAL_ERROR,
            SERVER_ERROR,
            RECONNECT;

            private WebSocketCloseNotification() {
            }
        }
    }
}
