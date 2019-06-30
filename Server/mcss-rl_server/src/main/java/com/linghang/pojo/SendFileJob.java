package com.linghang.pojo;

import com.linghang.rpc.client.SendFileClient;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.CountDownLatch;

public class SendFileJob implements Runnable {

    private SendFileJobDescription jobDescription;
    private CountDownLatch sendCdl;
    private NioEventLoopGroup group;

    public SendFileJob(String filePath, String fileName, SendPosition localSendPos, SendPosition remoteSendPos,
                       String host, CountDownLatch sendCdl, NioEventLoopGroup group) {
        jobDescription = new SendFileJobDescription(filePath, fileName, localSendPos, remoteSendPos, host);
        this.sendCdl = sendCdl;
        this.group = group;
    }

    @Override
    public void run() {
        // create client
        SendFileClient sendClient = new SendFileClient(jobDescription, sendCdl, group);
        try {
            sendClient.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("======== CLIENT SEND FAILED =========");
        }
    }
}
