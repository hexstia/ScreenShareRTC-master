//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.tavendo.autobahn;

import android.net.SSLCertificateSocketFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.net.URI;

import javax.net.SocketFactory;

import de.tavendo.autobahn.WebSocket.WebSocketConnectionObserver.WebSocketCloseNotification;
import de.tavendo.autobahn.WebSocketMessage.BinaryMessage;
import de.tavendo.autobahn.WebSocketMessage.ClientHandshake;
import de.tavendo.autobahn.WebSocketMessage.Close;
import de.tavendo.autobahn.WebSocketMessage.ConnectionLost;
import de.tavendo.autobahn.WebSocketMessage.Error;
import de.tavendo.autobahn.WebSocketMessage.Ping;
import de.tavendo.autobahn.WebSocketMessage.Pong;
import de.tavendo.autobahn.WebSocketMessage.ProtocolViolation;
import de.tavendo.autobahn.WebSocketMessage.Quit;
import de.tavendo.autobahn.WebSocketMessage.RawTextMessage;
import de.tavendo.autobahn.WebSocketMessage.ServerError;
import de.tavendo.autobahn.WebSocketMessage.ServerHandshake;
import de.tavendo.autobahn.WebSocketMessage.TextMessage;

public class WebSocketConnection implements WebSocket {
    private static final String TAG = WebSocketConnection.class.getName();
    private static final String WS_URI_SCHEME = "ws";
    private static final String WSS_URI_SCHEME = "wss";
    private static final String WS_WRITER = "WebSocketWriter";
    private static final String WS_READER = "WebSocketReader";
    private final Handler mHandler;
    private WebSocketReader mWebSocketReader;
    private WebSocketWriter mWebSocketWriter;
    private Socket mSocket;
    private WebSocketConnection.SocketThread mSocketThread;
    private URI mWebSocketURI;
    private String[] mWebSocketSubprotocols;
    private WeakReference<WebSocketConnectionObserver> mWebSocketConnectionObserver;
    private WebSocketOptions mWebSocketOptions;
    private boolean mPreviousConnection = false;

    public WebSocketConnection() {
        Log.d(TAG, "WebSocket connection created.");
        this.mHandler = new WebSocketConnection.ThreadHandler(this);
    }

    public void sendTextMessage(String payload) {
        this.mWebSocketWriter.forward(new TextMessage(payload));
    }

    public void sendRawTextMessage(byte[] payload) {
        this.mWebSocketWriter.forward(new RawTextMessage(payload));
    }

    public void sendBinaryMessage(byte[] payload) {
        this.mWebSocketWriter.forward(new BinaryMessage(payload));
    }

    public boolean isConnected() {
        return this.mSocket != null && this.mSocket.isConnected() && !this.mSocket.isClosed();
    }

    private void failConnection(WebSocketCloseNotification code, String reason) {
        Log.d(TAG, "fail connection [code = " + code + ", reason = " + reason);
        if (this.mWebSocketReader != null) {
            this.mWebSocketReader.quit();

            try {
                this.mWebSocketReader.join();
            } catch (InterruptedException var5) {
                var5.printStackTrace();
            }
        } else {
            Log.d(TAG, "mReader already NULL");
        }

        if (this.mWebSocketWriter != null) {
            this.mWebSocketWriter.forward(new Quit());

            try {
                this.mWebSocketWriter.join();
            } catch (InterruptedException var4) {
                var4.printStackTrace();
            }
        } else {
            Log.d(TAG, "mWriter already NULL");
        }

        if (this.mSocket != null) {
            this.mSocketThread.getHandler().post(new Runnable() {
                public void run() {
                    WebSocketConnection.this.mSocketThread.stopConnection();
                }
            });
        } else {
            Log.d(TAG, "mTransportChannel already NULL");
        }

        this.mSocketThread.getHandler().post(new Runnable() {
            public void run() {
                Looper.myLooper().quit();
            }
        });
        this.onClose(code, reason);
        Log.d(TAG, "worker threads stopped");
    }

    public void connect(URI webSocketURI, WebSocketConnectionObserver connectionObserver) throws WebSocketException {
        this.connect(webSocketURI, connectionObserver, new WebSocketOptions());
    }

    public void connect(URI webSocketURI, WebSocketConnectionObserver connectionObserver, WebSocketOptions options) throws WebSocketException {
        this.connect(webSocketURI, (String[])null, connectionObserver, options);
    }

    public void connect(URI webSocketURI, String[] subprotocols, WebSocketConnectionObserver connectionObserver, WebSocketOptions options) throws WebSocketException {
        if (this.isConnected()) {
            throw new WebSocketException("already connected");
        } else if (webSocketURI == null) {
            throw new WebSocketException("WebSockets URI null.");
        } else {
            this.mWebSocketURI = webSocketURI;
            if (!this.mWebSocketURI.getScheme().equals("ws") && !this.mWebSocketURI.getScheme().equals("wss")) {
                throw new WebSocketException("unsupported scheme for WebSockets URI");
            } else {
                this.mWebSocketSubprotocols = subprotocols;
                this.mWebSocketConnectionObserver = new WeakReference(connectionObserver);
                this.mWebSocketOptions = new WebSocketOptions(options);
                this.connect();
            }
        }
    }

    public void disconnect() {
        if (this.mWebSocketWriter != null && this.mWebSocketWriter.isAlive()) {
            this.mWebSocketWriter.forward(new Close());
        } else {
            Log.d(TAG, "Could not send WebSocket Close .. writer already null");
        }

        this.mPreviousConnection = false;
    }

    public boolean reconnect() {
        if (!this.isConnected() && this.mWebSocketURI != null) {
            this.connect();
            return true;
        } else {
            return false;
        }
    }

    private void connect() {
        this.mSocketThread = new WebSocketConnection.SocketThread(this.mWebSocketURI, this.mWebSocketOptions);
        this.mSocketThread.start();
        WebSocketConnection.SocketThread var1 = this.mSocketThread;
        synchronized(this.mSocketThread) {
            try {
                this.mSocketThread.wait();
            } catch (InterruptedException var6) {
            }
        }

        this.mSocketThread.getHandler().post(new Runnable() {
            public void run() {
                WebSocketConnection.this.mSocketThread.startConnection();
            }
        });
        var1 = this.mSocketThread;
        synchronized(this.mSocketThread) {
            try {
                this.mSocketThread.wait();
            } catch (InterruptedException var4) {
            }
        }

        this.mSocket = this.mSocketThread.getSocket();
        if (this.mSocket == null) {
            this.onClose(WebSocketCloseNotification.CANNOT_CONNECT, this.mSocketThread.getFailureMessage());
        } else if (this.mSocket.isConnected()) {
            try {
                this.createReader();
                this.createWriter();
                ClientHandshake clientHandshake = new ClientHandshake(this.mWebSocketURI, (URI)null, this.mWebSocketSubprotocols);
                this.mWebSocketWriter.forward(clientHandshake);
            } catch (Exception var3) {
                this.onClose(WebSocketCloseNotification.INTERNAL_ERROR, var3.getLocalizedMessage());
            }
        } else {
            this.onClose(WebSocketCloseNotification.CANNOT_CONNECT, "could not connect to WebSockets server");
        }

    }

    protected boolean scheduleReconnect() {
        int interval = this.mWebSocketOptions.getReconnectInterval();
        boolean shouldReconnect = this.mSocket != null && this.mSocket.isConnected() && this.mPreviousConnection && interval > 0;
        if (shouldReconnect) {
            Log.d(TAG, "WebSocket reconnection scheduled");
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    Log.d(WebSocketConnection.TAG, "WebSocket reconnecting...");
                    WebSocketConnection.this.reconnect();
                }
            }, (long)interval);
        }

        return shouldReconnect;
    }

    private void onClose(WebSocketCloseNotification code, String reason) {
        boolean reconnecting = false;
        if (code == WebSocketCloseNotification.CANNOT_CONNECT || code == WebSocketCloseNotification.CONNECTION_LOST) {
            reconnecting = this.scheduleReconnect();
        }

        WebSocketConnectionObserver webSocketObserver = (WebSocketConnectionObserver)this.mWebSocketConnectionObserver.get();
        if (webSocketObserver != null) {
            try {
                if (reconnecting) {
                    webSocketObserver.onClose(WebSocketCloseNotification.RECONNECT, reason);
                } else {
                    webSocketObserver.onClose(code, reason);
                }
            } catch (Exception var6) {
                var6.printStackTrace();
            }
        } else {
            Log.d(TAG, "WebSocketObserver null");
        }

    }

    protected void processAppMessage(Object message) {
    }

    protected void createWriter() {
        this.mWebSocketWriter = new WebSocketWriter(this.mHandler, this.mSocket, this.mWebSocketOptions, "WebSocketWriter");
        this.mWebSocketWriter.start();
        WebSocketWriter var1 = this.mWebSocketWriter;
        synchronized(this.mWebSocketWriter) {
            try {
                this.mWebSocketWriter.wait();
            } catch (InterruptedException var3) {
            }
        }

        Log.d(TAG, "WebSocket writer created and started.");
    }

    protected void createReader() {
        this.mWebSocketReader = new WebSocketReader(this.mHandler, this.mSocket, this.mWebSocketOptions, "WebSocketReader");
        this.mWebSocketReader.start();
        WebSocketReader var1 = this.mWebSocketReader;
        synchronized(this.mWebSocketReader) {
            try {
                this.mWebSocketReader.wait();
            } catch (InterruptedException var3) {
            }
        }

        Log.d(TAG, "WebSocket reader created and started.");
    }

    private void handleMessage(Message message) {
        WebSocketConnectionObserver webSocketObserver = (WebSocketConnectionObserver)this.mWebSocketConnectionObserver.get();
        if (message.obj instanceof TextMessage) {
            TextMessage textMessage = (TextMessage)message.obj;
            if (webSocketObserver != null) {
                webSocketObserver.onTextMessage(textMessage.mPayload);
            } else {
                Log.d(TAG, "could not call onTextMessage() .. handler already NULL");
            }
        } else if (message.obj instanceof RawTextMessage) {
            RawTextMessage rawTextMessage = (RawTextMessage)message.obj;
            if (webSocketObserver != null) {
                webSocketObserver.onRawTextMessage(rawTextMessage.mPayload);
            } else {
                Log.d(TAG, "could not call onRawTextMessage() .. handler already NULL");
            }
        } else if (message.obj instanceof BinaryMessage) {
            BinaryMessage binaryMessage = (BinaryMessage)message.obj;
            if (webSocketObserver != null) {
                webSocketObserver.onBinaryMessage(binaryMessage.mPayload);
            } else {
                Log.d(TAG, "could not call onBinaryMessage() .. handler already NULL");
            }
        } else if (message.obj instanceof Ping) {
            Ping ping = (Ping)message.obj;
            Log.d(TAG, "WebSockets Ping received");
            Pong pong = new Pong();
            pong.mPayload = ping.mPayload;
            this.mWebSocketWriter.forward(pong);
        } else if (message.obj instanceof Pong) {
            Pong pong = (Pong)message.obj;
            Log.d(TAG, "WebSockets Pong received" + pong.mPayload);
        } else if (message.obj instanceof Close) {
            Close close = (Close)message.obj;
            Log.d(TAG, "WebSockets Close received (" + close.getCode() + " - " + close.getReason() + ")");
            this.mWebSocketWriter.forward(new Close(1000));
        } else if (message.obj instanceof ServerHandshake) {
            ServerHandshake serverHandshake = (ServerHandshake)message.obj;
            Log.d(TAG, "opening handshake received");
            if (serverHandshake.mSuccess) {
                if (webSocketObserver != null) {
                    webSocketObserver.onOpen();
                } else {
                    Log.d(TAG, "could not call onOpen() .. handler already NULL");
                }

                this.mPreviousConnection = true;
            }
        } else if (message.obj instanceof ConnectionLost) {
            this.failConnection(WebSocketCloseNotification.CONNECTION_LOST, "WebSockets connection lost");
        } else if (message.obj instanceof ProtocolViolation) {
            this.failConnection(WebSocketCloseNotification.PROTOCOL_ERROR, "WebSockets protocol violation");
        } else if (message.obj instanceof Error) {
            Error error = (Error)message.obj;
            this.failConnection(WebSocketCloseNotification.INTERNAL_ERROR, "WebSockets internal error (" + error.mException.toString() + ")");
        } else if (message.obj instanceof ServerError) {
            ServerError error = (ServerError)message.obj;
            this.failConnection(WebSocketCloseNotification.SERVER_ERROR, "Server error " + error.mStatusCode + " (" + error.mStatusMessage + ")");
        } else {
            this.processAppMessage(message.obj);
        }

    }

    public static class SocketThread extends Thread {
        private static final String WS_CONNECTOR = "WebSocketConnector";
        private final URI mWebSocketURI;
        private Socket mSocket = null;
        private String mFailureMessage = null;
        private Handler mHandler;

        public SocketThread(URI uri, WebSocketOptions options) {
            this.setName("WebSocketConnector");
            this.mWebSocketURI = uri;
        }

        public void run() {
            Looper.prepare();
            this.mHandler = new Handler();
            synchronized(this) {
                this.notifyAll();
            }

            Looper.loop();
            Log.d(WebSocketConnection.TAG, "SocketThread exited.");
        }

        public void startConnection() {
            try {
                String host = this.mWebSocketURI.getHost();
                int port = this.mWebSocketURI.getPort();
                if (port == -1) {
                    if (this.mWebSocketURI.getScheme().equals("wss")) {
                        port = 443;
                    } else {
                        port = 80;
                    }
                }

                SocketFactory factory = null;
                if (this.mWebSocketURI.getScheme().equalsIgnoreCase("wss")) {
                    factory = SSLCertificateSocketFactory.getDefault();
                } else {
                    factory = SocketFactory.getDefault();
                }

                this.mSocket = factory.createSocket(host, port);
            } catch (IOException var5) {
                this.mFailureMessage = var5.getLocalizedMessage();
            }

            synchronized(this) {
                this.notifyAll();
            }
        }

        public void stopConnection() {
            try {
                this.mSocket.close();
                this.mSocket = null;
            } catch (IOException var2) {
                this.mFailureMessage = var2.getLocalizedMessage();
            }

        }

        public Handler getHandler() {
            return this.mHandler;
        }

        public Socket getSocket() {
            return this.mSocket;
        }

        public String getFailureMessage() {
            return this.mFailureMessage;
        }
    }

    private static class ThreadHandler extends Handler {
        private final WeakReference<WebSocketConnection> mWebSocketConnection;

        public ThreadHandler(WebSocketConnection webSocketConnection) {
            this.mWebSocketConnection = new WeakReference(webSocketConnection);
        }

        public void handleMessage(Message message) {
            WebSocketConnection webSocketConnection = (WebSocketConnection)this.mWebSocketConnection.get();
            if (webSocketConnection != null) {
                webSocketConnection.handleMessage(message);
            }

        }
    }
}
