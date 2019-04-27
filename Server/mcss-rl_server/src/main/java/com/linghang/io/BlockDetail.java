package com.linghang.io;

import java.io.Serializable;

public class BlockDetail implements Serializable {

    private static final long serialVersionUID = 2254153731312156649L;

    private String fileName;
    private long startPos;
    private int readByte;
    private byte[] bytes;

    public BlockDetail(){}

    public BlockDetail(String fileName, Long startPos, Integer readByte) {
        this.fileName = fileName;
        this.startPos = startPos;
        this.readByte = readByte;
    }

    @Override
    public String toString() {
        return "BlockDetail{" +
                "fileName='" + fileName + '\'' +
                ", startPos=" + startPos +
                ", endPos=" + readByte +
                '}';
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getStartPos() {
        return startPos;
    }

    public void setStartPos(long startPos) {
        this.startPos = startPos;
    }

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
