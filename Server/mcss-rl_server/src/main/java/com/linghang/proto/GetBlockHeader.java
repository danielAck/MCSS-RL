package com.linghang.proto;

import java.io.Serializable;

public class GetBlockHeader implements Serializable {

    private static final long serialVersionUID = 7822926292703803465L;

    private String fileName;
    private long startPos;
    private long length;

    public GetBlockHeader(String fileName, long startPos, long length) {
        this.fileName = fileName;
        this.startPos = startPos;
        this.length = length;
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

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}
