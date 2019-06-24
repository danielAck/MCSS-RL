package com.linghang.pojo;

public class UploadDetail {

    private Integer id;
    private String fileName;
    private Integer cloudId;
    private String  ip;

    public UploadDetail(Integer id, String fileName, Integer cloudId, String ip) {
        this.id = id;
        this.fileName = fileName;
        this.cloudId = cloudId;
        this.ip = ip;
    }

    public UploadDetail(String fileName, Integer cloudId, String ip) {
        this.fileName = fileName;
        this.cloudId = cloudId;
        this.ip = ip;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getCloudId() {
        return cloudId;
    }

    public void setCloudId(Integer cloudId) {
        this.cloudId = cloudId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
