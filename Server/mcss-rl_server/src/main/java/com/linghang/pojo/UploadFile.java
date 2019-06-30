package com.linghang.pojo;

public class UploadFile {

    private Integer id;
    private String fileName;
    private String subfix;
    private Long length;
    private Integer status;

    public UploadFile(Integer id, String fileName, String subfix, Long length, Integer status) {
        this.id = id;
        this.fileName = fileName;
        this.subfix = subfix;
        this.length = length;
        this.status = status;
    }

    public UploadFile(String fileName, String subfix, Long length, Integer status) {
        this.fileName = fileName;
        this.subfix = subfix;
        this.length = length;
        this.status = status;
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

    public String getSubfix() {
        return subfix;
    }

    public void setSubfix(String subfix) {
        this.subfix = subfix;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
