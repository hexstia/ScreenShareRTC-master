//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.tavendo.autobahn;

import java.net.URI;

public class WebSocketMessage {
    public WebSocketMessage() {
    }

    public static class BinaryMessage extends WebSocketMessage.Message {
        public byte[] mPayload;

        BinaryMessage(byte[] payload) {
            this.mPayload = payload;
        }
    }

    public static class ClientHandshake extends WebSocketMessage.Message {
        private final URI mURI;
        private final URI mOrigin;
        private final String[] mSubprotocols;

        ClientHandshake(URI uri) {
            this.mURI = uri;
            this.mOrigin = null;
            this.mSubprotocols = null;
        }

        ClientHandshake(URI uri, URI origin, String[] subprotocols) {
            this.mURI = uri;
            this.mOrigin = origin;
            this.mSubprotocols = subprotocols;
        }

        public URI getURI() {
            return this.mURI;
        }

        public URI getOrigin() {
            return this.mOrigin;
        }

        public String[] getSubprotocols() {
            return this.mSubprotocols;
        }
    }

    public static class Close extends WebSocketMessage.Message {
        private int mCode;
        private String mReason;

        Close() {
            this.mCode = 1011;
            this.mReason = null;
        }

        Close(int code) {
            this.mCode = code;
            this.mReason = null;
        }

        Close(int code, String reason) {
            this.mCode = code;
            this.mReason = reason;
        }

        public int getCode() {
            return this.mCode;
        }

        public String getReason() {
            return this.mReason;
        }
    }

    public static class ConnectionLost extends WebSocketMessage.Message {
        public ConnectionLost() {
        }
    }

    public static class Error extends WebSocketMessage.Message {
        public Exception mException;

        public Error(Exception e) {
            this.mException = e;
        }
    }

    public static class Message {
        public Message() {
        }
    }

    public static class Ping extends WebSocketMessage.Message {
        public byte[] mPayload;

        Ping() {
            this.mPayload = null;
        }

        Ping(byte[] payload) {
            this.mPayload = payload;
        }
    }

    public static class Pong extends WebSocketMessage.Message {
        public byte[] mPayload;

        Pong() {
            this.mPayload = null;
        }

        Pong(byte[] payload) {
            this.mPayload = payload;
        }
    }

    public static class ProtocolViolation extends WebSocketMessage.Message {
        public WebSocketException mException;

        public ProtocolViolation(WebSocketException e) {
            this.mException = e;
        }
    }

    public static class Quit extends WebSocketMessage.Message {
        public Quit() {
        }
    }

    public static class RawTextMessage extends WebSocketMessage.Message {
        public byte[] mPayload;

        RawTextMessage(byte[] payload) {
            this.mPayload = payload;
        }
    }

    public static class ServerError extends WebSocketMessage.Message {
        public int mStatusCode;
        public String mStatusMessage;

        public ServerError(int statusCode, String statusMessage) {
            this.mStatusCode = statusCode;
            this.mStatusMessage = statusMessage;
        }
    }

    public static class ServerHandshake extends WebSocketMessage.Message {
        public boolean mSuccess;

        public ServerHandshake(boolean success) {
            this.mSuccess = success;
        }
    }

    public static class TextMessage extends WebSocketMessage.Message {
        public String mPayload;

        TextMessage(String payload) {
            this.mPayload = payload;
        }
    }

    public static class WebSocketCloseCode {
        public static final int NORMAL = 1000;
        public static final int ENDPOINT_GOING_AWAY = 1001;
        public static final int ENDPOINT_PROTOCOL_ERROR = 1002;
        public static final int ENDPOINT_UNSUPPORTED_DATA_TYPE = 1003;
        public static final int RESERVED = 1004;
        public static final int RESERVED_NO_STATUS = 1005;
        public static final int RESERVED_NO_CLOSING_HANDSHAKE = 1006;
        public static final int ENDPOINT_BAD_DATA = 1007;
        public static final int POLICY_VIOLATION = 1008;
        public static final int MESSAGE_TOO_BIG = 1009;
        public static final int ENDPOINT_NEEDS_EXTENSION = 1010;
        public static final int UNEXPECTED_CONDITION = 1011;
        public static final int RESERVED_TLS_REQUIRED = 1015;

        public WebSocketCloseCode() {
        }
    }
}
