package com.linghang.pojo;

public class SendPosition {

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
