package com.linghang.proto;

import java.io.Serializable;

public class GetBlockHeader implements Serializable {

    private static final long serialVersionUID = 7822926292703803465L;

    private String remoteFileName;
    private String remoteFilePath;
    private long startPos;
    private long length;

    public GetBlockHeader(String remoteFileName, String remoteFilePath, long startPos, long length) {
        this.remoteFileName = remoteFileName;
        this.remoteFilePath = remoteFilePath;
        this.startPos = startPos;
        this.length = length;
    }

    public String getRemoteFileName() {
        return remoteFileName;
    }

    public void setRemoteFileName(String remoteFileName) {
        this.remoteFileName = remoteFileName;
    }

    public String getRemoteFilePath() {
        return remoteFilePath;
    }

    public void setRemoteFilePath(String remoteFilePath) {
        this.remoteFilePath = remoteFilePath;
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

    @Override
    public String toString() {
        return "GetBlockHeader{" +
                "remoteFileName='" + remoteFileName + '\'' +
                ", remoteFilePath='" + remoteFilePath + '\'' +
                ", startPos=" + startPos +
                ", length=" + length +
                '}';
    }
}
