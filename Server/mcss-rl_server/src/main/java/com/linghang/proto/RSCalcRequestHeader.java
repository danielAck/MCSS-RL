package com.linghang.proto;

import java.io.Serializable;

public class RSCalcRequestHeader implements Serializable {

    private static final long serialVersionUID = 388993274649025442L;

    private String fileName;
    private long startPos;

    public RSCalcRequestHeader(String fileName, long startPos) {
        this.fileName = fileName;
        this.startPos = startPos;
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
}
