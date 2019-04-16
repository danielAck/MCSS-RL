package com.linghang.pojo;

import com.linghang.rpc.client.SendFileClient;

public class SendFileJob extends Thread implements Job {

    private SendFileJobDescription jobDescription;

    public SendFileJob(String filePath, String fileName,
                       SendPosition sendPosition, String host, int port) {
        jobDescription = new SendFileJobDescription(filePath, fileName, sendPosition, host, port);
    }

    @Override
    public void start(){
        // create client
        SendFileClient sendClient = new SendFileClient(jobDescription);
        try {
            sendClient.start();
            System.out.println("======== CLIENT SEND FINISH =========");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("======== CLIENT SEND FAILED =========");
        }
    }

    // 多线程
    @Override
    public void run() {

    }
}
