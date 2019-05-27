package com.linghang.pojo;

import java.io.Serializable;

public class SendPosition implements Serializable {

    private static final long serialVersionUID = 4172141707802971001L;

    public long getStartPos() {
        return startPos;
    }

    public void setStartPos(long startPos) {
        this.startPos = startPos;
    }

    public long getEndPos() {
        return endPos;
    }

    public void setEndPos(long endPos) {
        this.endPos = endPos;
    }

    private long startPos;
    private long endPos;

    public SendPosition(long startPos, long endPos) {
        this.startPos = startPos;
        this.endPos = endPos;
    }


}
