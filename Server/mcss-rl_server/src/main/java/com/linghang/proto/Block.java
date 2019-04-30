package com.linghang.proto;

import java.io.Serializable;

public class Block implements Serializable {

    private static final long serialVersionUID = 8128462135147930381L;
    private int readByte;
    private byte[] bytes;

    public Block() {}

    public int getReadByte() {
        return readByte;
    }

    public void setReadByte(int readByte) {
        this.readByte = readByte;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
