package com.linghang.proto;

import java.io.Serializable;

public class Block implements Serializable {

    private static final long serialVersionUID = 8128462135147930381L;
    private long startPos;
    private byte[] bytes;

    public Block(long startPos, byte[] bytes) {
        this.startPos = startPos;
        this.bytes = bytes;
    }

    public long getStartPos() {
        return startPos;
    }

    public void setStartPos(long startPos) {
        this.startPos = startPos;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
