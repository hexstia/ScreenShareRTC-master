//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.tavendo.autobahn;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {
    private final ByteBuffer mBuffer;

    public ByteBufferInputStream(ByteBuffer buffer) {
        this.mBuffer = buffer;
    }

    public synchronized int read() throws IOException {
        return !this.mBuffer.hasRemaining() ? -1 : this.mBuffer.get() & 255;
    }

    public synchronized int read(byte[] bytes, int off, int len) throws IOException {
        if (bytes == null) {
            throw new NullPointerException();
        } else if (off >= 0 && len >= 0 && len <= bytes.length - off) {
            if (len == 0) {
                return 0;
            } else {
                int length = Math.min(this.mBuffer.remaining(), len);
                if (length == 0) {
                    return -1;
                } else {
                    this.mBuffer.get(bytes, off, length);
                    return length;
                }
            }
        } else {
            throw new IndexOutOfBoundsException();
        }
    }
}
