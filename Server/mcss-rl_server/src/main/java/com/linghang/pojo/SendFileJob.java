package com.linghang.pojo;

public class SendFileJob extends Thread implements Job {

    private SendFileJobDescription jobDescription;

    public SendFileJob(String filePath, String fileName,
                       SendPosition sendPosition, String host, int port) {
        jobDescription = new SendFileJobDescription(filePath, fileName, sendPosition, host, port);
    }

    @Override
    public void start() {

    }

    // 多线程
    @Override
    public void run() {

    }
}
