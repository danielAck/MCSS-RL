package com.linghang.pojo;

import java.io.File;

public class SendFileJobDescription extends JobDescription{

    private String filePath;
    private String fileName;
    private SendPosition sendPosition;
    private String host;

    public SendFileJobDescription(String filePath, String fileName, SendPosition sendPosition, String host) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.sendPosition = sendPosition;
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

    public SendPosition getSendPosition() {
        return sendPosition;
    }

    public void setSendPosition(SendPosition sendPosition) {
        this.sendPosition = sendPosition;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

}
