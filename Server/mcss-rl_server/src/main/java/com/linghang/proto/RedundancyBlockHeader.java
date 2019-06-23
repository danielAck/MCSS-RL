package com.linghang.proto;

import java.io.Serializable;

public class RedundancyBlockHeader implements Serializable {

    private static final long serialVersionUID = 4723869160317851740L;

    private String remoteFileName;
    private String remoteFilePath;
    private long startPos;

    public RedundancyBlockHeader(String remoteFileName, String remoteFilePath, long startPos) {
        this.remoteFileName = remoteFileName;
        this.remoteFilePath = remoteFilePath;
        this.startPos = startPos;
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

    @Override
    public String toString() {
        return "RedundancyBlockHeader{" +
                "remoteFileName='" + remoteFileName + '\'' +
                ", remoteFilePath='" + remoteFilePath + '\'' +
                ", startPos=" + startPos +
                '}';
    }
}
