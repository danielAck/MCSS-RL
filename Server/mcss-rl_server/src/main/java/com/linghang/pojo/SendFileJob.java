package com.linghang.pojo;

public class SendFileJob extends Thread implements Job {

    private String filePath;
    private String fileName;
    private int startPos;
    private int endPos;
    private String host;
    private int blockIdx;
    private int port;

    public SendFileJob(String filePath, String fileName, int blockIdx,
                       int startPos, int endPos, String host, int port) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.blockIdx = blockIdx;
        this.startPos = startPos;
        this.endPos = endPos;
        this.host = host;
        this.port = port;
    }

    @Override
    public void start() {

    }

    // 多线程
    @Override
    public void run() {

    }
}
