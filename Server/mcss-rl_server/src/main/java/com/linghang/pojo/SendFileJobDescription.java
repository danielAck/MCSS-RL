package com.linghang.pojo;

import java.io.File;

public class SendFileJobDescription extends JobDescription{

    private String filePath;
    private String fileName;
    private long startPos;
    private long endPos;

    public SendFileJobDescription(String filePath, String fileName, long startPos, long endPos) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.startPos = startPos;
        this.endPos = endPos;
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

    public long getStartPos() {
        return startPos;
    }

    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    public long getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

}
