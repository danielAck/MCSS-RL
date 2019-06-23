package com.linghang.proto;

import com.linghang.pojo.SendPosition;

import java.io.Serializable;

public class BlockHeader implements Serializable {

    private static final long serialVersionUID = 6917782500338669141L;

    private String remoteFileName;
    private String remoteFilePath;
    private SendPosition sendPosition;

    public BlockHeader(String remoteFileName, String remoteFilePath, SendPosition sendPosition) {
        this.remoteFileName = remoteFileName;
        this.remoteFilePath = remoteFilePath;
        this.sendPosition = sendPosition;
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

    public SendPosition getSendPosition() {
        return sendPosition;
    }

    public void setSendPosition(SendPosition sendPosition) {
        this.sendPosition = sendPosition;
    }

    @Override
    public String toString() {
        return "BlockHeader{" +
                "remoteFileName='" + remoteFileName + '\'' +
                ", remoteFilePath='" + remoteFilePath + '\'' +
                ", sendPosition=" + sendPosition.toString() +
                '}';
    }
}
