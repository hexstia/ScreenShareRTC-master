//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.tavendo.autobahn;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import de.tavendo.autobahn.WebSocketMessage.BinaryMessage;
import de.tavendo.autobahn.WebSocketMessage.Close;
import de.tavendo.autobahn.WebSocketMessage.ConnectionLost;
import de.tavendo.autobahn.WebSocketMessage.Error;
import de.tavendo.autobahn.WebSocketMessage.Ping;
import de.tavendo.autobahn.WebSocketMessage.Pong;
import de.tavendo.autobahn.WebSocketMessage.ProtocolViolation;
import de.tavendo.autobahn.WebSocketMessage.RawTextMessage;
import de.tavendo.autobahn.WebSocketMessage.ServerError;
import de.tavendo.autobahn.WebSocketMessage.ServerHandshake;
import de.tavendo.autobahn.WebSocketMessage.TextMessage;

public class WebSocketReader extends Thread {
    private static final String TAG = WebSocketReader.class.getCanonicalName();
    private final Handler mWebSocketConnectionHandler;
    private final Socket mSocket;
    private InputStream mInputStream;
    private final WebSocketOptions mWebSocketOptions;
    private volatile boolean mStopped = false;
    private final byte[] mNetworkBuffer;
    private final ByteBuffer mApplicationBuffer;
    private NoCopyByteArrayOutputStream mMessagePayload;
    private WebSocketReader.ReaderState mState;
    private boolean mInsideMessage = false;
    private int mMessageOpcode;
    private WebSocketFrameHeader mFrameHeader;
    private Utf8Validator mUTF8Validator = new Utf8Validator();

    public WebSocketReader(Handler master, Socket socket, WebSocketOptions options, String threadName) {
        super(threadName);
        this.mWebSocketConnectionHandler = master;
        this.mSocket = socket;
        this.mWebSocketOptions = options;
        this.mNetworkBuffer = new byte[4096];
        this.mApplicationBuffer = ByteBuffer.allocateDirect(options.getMaxFramePayloadSize() + 14);
        this.mMessagePayload = new NoCopyByteArrayOutputStream(options.getMaxMessagePayloadSize());
        this.mFrameHeader = null;
        this.mState = WebSocketReader.ReaderState.STATE_CONNECTING;
        Log.d(TAG, "WebSocket reader created.");
    }

    public void quit() {
        this.mStopped = true;
        Log.d(TAG, "quit");
    }

    protected void notify(Object message) {
        Message msg = this.mWebSocketConnectionHandler.obtainMessage();
        msg.obj = message;
        this.mWebSocketConnectionHandler.sendMessage(msg);
    }

    private boolean processData() throws Exception {
        int rsv;
        if (this.mFrameHeader == null) {
            if (this.mApplicationBuffer.position() >= 2) {
                byte b0 = this.mApplicationBuffer.get(0);
                boolean fin = (b0 & 128) != 0;
                rsv = (b0 & 112) >> 4;
                int opcode = b0 & 15;
                byte b1 = this.mApplicationBuffer.get(1);
                boolean masked = (b1 & 128) != 0;
                int payload_len1 = b1 & 127;
                if (rsv != 0) {
                    throw new WebSocketException("RSV != 0 and no extension negotiated");
                } else if (masked) {
                    throw new WebSocketException("masked server frame");
                } else {
                    if (opcode > 7) {
                        if (!fin) {
                            throw new WebSocketException("fragmented control frame");
                        }

                        if (payload_len1 > 125) {
                            throw new WebSocketException("control frame with payload length > 125 octets");
                        }

                        if (opcode != 8 && opcode != 9 && opcode != 10) {
                            throw new WebSocketException("control frame using reserved opcode " + opcode);
                        }

                        if (opcode == 8 && payload_len1 == 1) {
                            throw new WebSocketException("received close control frame with payload len 1");
                        }
                    } else {
                        if (opcode != 0 && opcode != 1 && opcode != 2) {
                            throw new WebSocketException("data frame using reserved opcode " + opcode);
                        }

                        if (!this.mInsideMessage && opcode == 0) {
                            throw new WebSocketException("received continuation data frame outside fragmented message");
                        }

                        if (this.mInsideMessage && opcode != 0) {
                            throw new WebSocketException("received non-continuation data frame while inside fragmented message");
                        }
                    }

                    int mask_len = masked ? 4 : 0;
                    int header_len;
                    if (payload_len1 < 126) {
                        header_len = 2 + mask_len;
                    } else if (payload_len1 == 126) {
                        header_len = 4 + mask_len;
                    } else {
                        if (payload_len1 != 127) {
                            throw new Exception("logic error");
                        }

                        header_len = 10 + mask_len;
                    }

                    if (this.mApplicationBuffer.position() >= header_len) {
                        int i = 2;
                        long payload_len = 0L;
                        if (payload_len1 == 126) {
                            payload_len = (long)((255 & this.mApplicationBuffer.get(i)) << 8 | 255 & this.mApplicationBuffer.get(i + 1));
                            if (payload_len < 126L) {
                                throw new WebSocketException("invalid data frame length (not using minimal length encoding)");
                            }

                            i += 2;
                        } else if (payload_len1 == 127) {
                            if ((128 & this.mApplicationBuffer.get(i + 0)) != 0) {
                                throw new WebSocketException("invalid data frame length (> 2^63)");
                            }

                            payload_len = (long)((255 & this.mApplicationBuffer.get(i + 0)) << 56 | (255 & this.mApplicationBuffer.get(i + 1)) << 48 | (255 & this.mApplicationBuffer.get(i + 2)) << 40 | (255 & this.mApplicationBuffer.get(i + 3)) << 32 | (255 & this.mApplicationBuffer.get(i + 4)) << 24 | (255 & this.mApplicationBuffer.get(i + 5)) << 16 | (255 & this.mApplicationBuffer.get(i + 6)) << 8 | 255 & this.mApplicationBuffer.get(i + 7));
                            if (payload_len < 65536L) {
                                throw new WebSocketException("invalid data frame length (not using minimal length encoding)");
                            }

                            i += 8;
                        } else {
                            payload_len = (long)payload_len1;
                        }

                        if (payload_len > (long)this.mWebSocketOptions.getMaxFramePayloadSize()) {
                            throw new WebSocketException("frame payload too large");
                        } else {
                            this.mFrameHeader = new WebSocketFrameHeader();
                            this.mFrameHeader.setOpcode(opcode);
                            this.mFrameHeader.setFin(fin);
                            this.mFrameHeader.setReserved(rsv);
                            this.mFrameHeader.setPayloadLength((int)payload_len);
                            this.mFrameHeader.setHeaderLength(header_len);
                            this.mFrameHeader.setTotalLen(this.mFrameHeader.getHeaderLength() + this.mFrameHeader.getPayloadLength());
                            if (masked) {
                                byte[] mask = new byte[4];

                                for(int j = 0; j < 4; ++j) {
                                    mask[i] = (byte)(255 & this.mApplicationBuffer.get(i + j));
                                }

                                this.mFrameHeader.setMask(mask);
                                i += 4;
                            } else {
                                this.mFrameHeader.setMask((byte[])null);
                            }

                            return this.mFrameHeader.getPayloadLength() == 0 || this.mApplicationBuffer.position() >= this.mFrameHeader.getTotalLength();
                        }
                    } else {
                        return false;
                    }
                }
            } else {
                return false;
            }
        } else if (this.mApplicationBuffer.position() >= this.mFrameHeader.getTotalLength()) {
            byte[] framePayload = null;
            int oldPosition = this.mApplicationBuffer.position();
            if (this.mFrameHeader.getPayloadLength() > 0) {
                framePayload = new byte[this.mFrameHeader.getPayloadLength()];
                this.mApplicationBuffer.position(this.mFrameHeader.getHeaderLength());
                this.mApplicationBuffer.get(framePayload, 0, this.mFrameHeader.getPayloadLength());
            }

            this.mApplicationBuffer.position(this.mFrameHeader.getTotalLength());
            this.mApplicationBuffer.limit(oldPosition);
            this.mApplicationBuffer.compact();
            if (this.mFrameHeader.getOpcode() > 7) {
                if (this.mFrameHeader.getOpcode() != 8) {
                    if (this.mFrameHeader.getOpcode() == 9) {
                        this.onPing(framePayload);
                    } else {
                        if (this.mFrameHeader.getOpcode() != 10) {
                            throw new Exception("logic error");
                        }

                        this.onPong(framePayload);
                    }
                } else {
                    rsv = 1005;
                    String reason = null;
                    if (this.mFrameHeader.getPayloadLength() >= 2) {
                        rsv = (framePayload[0] & 255) * 256 + (framePayload[1] & 255);
                        if (rsv < 1000 || rsv >= 1000 && rsv <= 2999 && rsv != 1000 && rsv != 1001 && rsv != 1002 && rsv != 1003 && rsv != 1007 && rsv != 1008 && rsv != 1009 && rsv != 1010 && rsv != 1011 || rsv >= 5000) {
                            throw new WebSocketException("invalid close code " + rsv);
                        }

                        if (this.mFrameHeader.getPayloadLength() > 2) {
                            byte[] ra = new byte[this.mFrameHeader.getPayloadLength() - 2];
                            System.arraycopy(framePayload, 2, ra, 0, this.mFrameHeader.getPayloadLength() - 2);
                            Utf8Validator val = new Utf8Validator();
                            val.validate(ra);
                            if (!val.isValid()) {
                                throw new WebSocketException("invalid close reasons (not UTF-8)");
                            }

                            reason = new String(ra, "UTF-8");
                        }
                    }

                    this.onClose(rsv, reason);
                }
            } else {
                if (!this.mInsideMessage) {
                    this.mInsideMessage = true;
                    this.mMessageOpcode = this.mFrameHeader.getOpcode();
                    if (this.mMessageOpcode == 1 && this.mWebSocketOptions.getValidateIncomingUtf8()) {
                        this.mUTF8Validator.reset();
                    }
                }

                if (framePayload != null) {
                    if (this.mMessagePayload.size() + framePayload.length > this.mWebSocketOptions.getMaxMessagePayloadSize()) {
                        throw new WebSocketException("message payload too large");
                    }

                    if (this.mMessageOpcode == 1 && this.mWebSocketOptions.getValidateIncomingUtf8() && !this.mUTF8Validator.validate(framePayload)) {
                        throw new WebSocketException("invalid UTF-8 in text message payload");
                    }

                    this.mMessagePayload.write(framePayload);
                }

                if (this.mFrameHeader.isFin()) {
                    if (this.mMessageOpcode == 1) {
                        if (this.mWebSocketOptions.getValidateIncomingUtf8() && !this.mUTF8Validator.isValid()) {
                            throw new WebSocketException("UTF-8 text message payload ended within Unicode code point");
                        }

                        if (this.mWebSocketOptions.getReceiveTextMessagesRaw()) {
                            this.onRawTextMessage(this.mMessagePayload.toByteArray());
                        } else {
                            String s = new String(this.mMessagePayload.toByteArray(), "UTF-8");
                            this.onTextMessage(s);
                        }
                    } else {
                        if (this.mMessageOpcode != 2) {
                            throw new Exception("logic error");
                        }

                        this.onBinaryMessage(this.mMessagePayload.toByteArray());
                    }

                    this.mInsideMessage = false;
                    this.mMessagePayload.reset();
                }
            }

            this.mFrameHeader = null;
            return this.mApplicationBuffer.position() > 0;
        } else {
            return false;
        }
    }

    protected void onHandshake(boolean success) {
        this.notify(new ServerHandshake(success));
    }

    protected void onClose(int code, String reason) {
        this.notify(new Close(code, reason));
    }

    protected void onPing(byte[] payload) {
        this.notify(new Ping(payload));
    }

    protected void onPong(byte[] payload) {
        this.notify(new Pong(payload));
    }

    protected void onTextMessage(String payload) {
        this.notify(new TextMessage(payload));
    }

    protected void onRawTextMessage(byte[] payload) {
        this.notify(new RawTextMessage(payload));
    }

    protected void onBinaryMessage(byte[] payload) {
        this.notify(new BinaryMessage(payload));
    }

    private boolean processHandshake() throws UnsupportedEncodingException {
        boolean res = false;

        for(int pos = this.mApplicationBuffer.position() - 4; pos >= 0; --pos) {
            if (this.mApplicationBuffer.get(pos + 0) == 13 && this.mApplicationBuffer.get(pos + 1) == 10 && this.mApplicationBuffer.get(pos + 2) == 13 && this.mApplicationBuffer.get(pos + 3) == 10) {
                int oldPosition = this.mApplicationBuffer.position();
                boolean serverError = false;
                if (this.mApplicationBuffer.get(0) == 72 && this.mApplicationBuffer.get(1) == 84 && this.mApplicationBuffer.get(2) == 84 && this.mApplicationBuffer.get(3) == 80) {
                    Pair<Integer, String> status = this.parseHTTPStatus();
                    if ((Integer)status.first >= 300) {
                        this.notify(new ServerError((Integer)status.first, (String)status.second));
                        serverError = true;
                    }
                }

                this.mApplicationBuffer.position(pos + 4);
                this.mApplicationBuffer.limit(oldPosition);
                this.mApplicationBuffer.compact();
                if (!serverError) {
                    res = this.mApplicationBuffer.position() > 0;
                    this.mState = WebSocketReader.ReaderState.STATE_OPEN;
                } else {
                    res = true;
                    this.mState = WebSocketReader.ReaderState.STATE_CLOSED;
                    this.mStopped = true;
                }

                this.onHandshake(!serverError);
                break;
            }
        }

        return res;
    }

    private Pair<Integer, String> parseHTTPStatus() throws UnsupportedEncodingException {
        int beg;
        for(beg = 4; beg < this.mApplicationBuffer.position() && this.mApplicationBuffer.get(beg) != 32; ++beg) {
        }

        int end;
        for(end = beg + 1; end < this.mApplicationBuffer.position() && this.mApplicationBuffer.get(end) != 32; ++end) {
        }

        ++beg;
        int statusCode = 0;

        int eol;
        int statusMessageLength;
        for(eol = 0; beg + eol < end; ++eol) {
            statusMessageLength = this.mApplicationBuffer.get(beg + eol) - 48;
            statusCode *= 10;
            statusCode += statusMessageLength;
        }

        ++end;

        for(eol = end; eol < this.mApplicationBuffer.position() && this.mApplicationBuffer.get(eol) != 13; ++eol) {
        }

        statusMessageLength = eol - end;
        byte[] statusBuf = new byte[statusMessageLength];
        this.mApplicationBuffer.position(end);
        this.mApplicationBuffer.get(statusBuf, 0, statusMessageLength);
        String statusMessage = new String(statusBuf, "UTF-8");
        Log.w(TAG, String.format("Status: %d (%s)", statusCode, statusMessage));
        return new Pair(statusCode, statusMessage);
    }

    private boolean consumeData() throws Exception {
        int a = this.mState.ordinal();
        Log.i("TAG",a+"");
        switch(this.mState.ordinal()) {
            case 1:
                return false;
            case 2:
                return this.processHandshake();
            case 3:
            case 4:
                return this.processData();
            default:
                return false;
        }
    }

    public void run() {
        synchronized(this) {
            this.notifyAll();
        }

        InputStream inputStream = null;

        try {
            inputStream = this.mSocket.getInputStream();
        } catch (IOException var3) {
            Log.e(TAG, var3.getLocalizedMessage());
            return;
        }

        this.mInputStream = inputStream;
        Log.d(TAG, "WebSocker reader running.");
        this.mApplicationBuffer.clear();

        while(!this.mStopped) {
            try {
                int bytesRead = this.mInputStream.read(this.mNetworkBuffer);
                if (bytesRead > 0) {
                    this.mApplicationBuffer.put(this.mNetworkBuffer, 0, bytesRead);

                    while(this.consumeData()) {
                    }
                } else if (bytesRead == -1) {
                    Log.d(TAG, "run() : ConnectionLost");
                    this.notify(new ConnectionLost());
                    this.mStopped = true;
                } else {
                    Log.e(TAG, "WebSocketReader read() failed.");
                }
            } catch (WebSocketException var5) {
                Log.d(TAG, "run() : WebSocketException (" + var5.toString() + ")");
                this.notify(new ProtocolViolation(var5));
            } catch (SocketException var6) {
                Log.d(TAG, "run() : SocketException (" + var6.toString() + ")");
                this.notify(new ConnectionLost());
            } catch (IOException var7) {
                Log.d(TAG, "run() : IOException (" + var7.toString() + ")");
                this.notify(new ConnectionLost());
            } catch (Exception var8) {
                Log.d(TAG, "run() : Exception (" + var8.toString() + ")");
                this.notify(new Error(var8));
            }
        }

        Log.d(TAG, "WebSocket reader ended.");
    }

    private static enum ReaderState {
        STATE_CLOSED,
        STATE_CONNECTING,
        STATE_CLOSING,
        STATE_OPEN;

        private ReaderState() {
        }
    }
}
