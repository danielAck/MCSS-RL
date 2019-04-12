package com.linghang.pojo;

import java.io.File;

public class SendFileJobDescription extends JobDescription{

    private String filePath;
    private String fileName;
    private SendPosition sendPosition;
    private String host;
    private int port;

    public SendFileJobDescription(String filePath, String fileName, SendPosition sendPosition, String host, int port) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.sendPosition = sendPosition;
        this.host = host;
        this.port = port;
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
