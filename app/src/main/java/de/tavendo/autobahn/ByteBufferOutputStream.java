//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.tavendo.autobahn;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class ByteBufferOutputStream extends OutputStream {
    private final int mInitialSize;
    private final int mGrowSize;
    private ByteBuffer mBuffer;

    public ByteBufferOutputStream() {
        this(131072, 65536);
    }

    public ByteBufferOutputStream(int initialSize, int growSize) {
        this.mInitialSize = initialSize;
        this.mGrowSize = growSize;
        this.mBuffer = ByteBuffer.allocateDirect(this.mInitialSize);
        this.mBuffer.clear();
    }

    public ByteBuffer getBuffer() {
        return this.mBuffer;
    }

    public Buffer flip() {
        return this.mBuffer.flip();
    }

    public Buffer clear() {
        return this.mBuffer.clear();
    }

    public int remaining() {
        return this.mBuffer.remaining();
    }

    public synchronized void expand(int requestSize) {
        if (requestSize > this.mBuffer.capacity()) {
            ByteBuffer oldBuffer = this.mBuffer;
            int oldPosition = this.mBuffer.position();
            int newCapacity = (requestSize / this.mGrowSize + 1) * this.mGrowSize;
            this.mBuffer = ByteBuffer.allocateDirect(newCapacity);
            oldBuffer.clear();
            this.mBuffer.clear();
            this.mBuffer.put(oldBuffer);
            this.mBuffer.position(oldPosition);
        }

    }

    public synchronized void write(int b) throws IOException {
        if (this.mBuffer.position() + 1 > this.mBuffer.capacity()) {
            this.expand(this.mBuffer.capacity() + 1);
        }

        this.mBuffer.put((byte)b);
    }

    public synchronized void write(byte[] bytes, int off, int len) throws IOException {
        if (this.mBuffer.position() + len > this.mBuffer.capacity()) {
            this.expand(this.mBuffer.capacity() + len);
        }

        this.mBuffer.put(bytes, off, len);
    }

    public synchronized void write(byte[] bytes) throws IOException {
        this.write(bytes, 0, bytes.length);
    }

    public synchronized void write(String str) throws IOException {
        this.write(str.getBytes("UTF-8"));
    }

    public synchronized void crlf() throws IOException {
        this.write(13);
        this.write(10);
    }
}
