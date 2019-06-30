package com.linghang.pojo;

/**
 * GUI部分用到
 */
public class CloudFile {

    private String fileName;
    private String ip;

    public CloudFile(String fileName, String ip) {
        this.fileName = fileName;
        this.ip = ip;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
