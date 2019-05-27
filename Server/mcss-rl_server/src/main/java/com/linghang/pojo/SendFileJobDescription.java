package com.linghang.pojo;

import java.io.File;

public class SendFileJobDescription extends JobDescription{

    private String filePath;
    private String fileName;
    private SendPosition localSendPos;
    private SendPosition remoteSendPos;
    private String host;

    public SendFileJobDescription(String filePath, String fileName, SendPosition localSendPos, SendPosition remoteSendPos, String host) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.localSendPos = localSendPos;
        this.remoteSendPos = remoteSendPos;
        this.host = host;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public SendPosition getLocalSendPos() {
        return localSendPos;
    }

    public void setLocalSendPos(SendPosition localSendPos) {
        this.localSendPos = localSendPos;
    }

    public SendPosition getRemoteSendPos() {
        return remoteSendPos;
    }

    public void setRemoteSendPos(SendPosition remoteSendPos) {
        this.remoteSendPos = remoteSendPos;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
