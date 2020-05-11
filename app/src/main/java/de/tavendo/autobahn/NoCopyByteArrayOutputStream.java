//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.tavendo.autobahn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class NoCopyByteArrayOutputStream extends ByteArrayOutputStream {
    public NoCopyByteArrayOutputStream() {
    }

    public NoCopyByteArrayOutputStream(int size) {
        super(size);
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(this.buf, 0, this.count);
    }

    public byte[] getByteArray() {
        return this.buf;
    }
}
