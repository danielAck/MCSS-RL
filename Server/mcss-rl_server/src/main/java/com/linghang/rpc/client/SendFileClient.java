package com.linghang.rpc.client;

import com.linghang.pojo.SendFileJobDescription;
import com.linghang.service.SendDataService;
import com.linghang.service.Service;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class SendFileClient {

    private SendFileJobDescription jobDescription;
    private Service sendService;
    private CountDownLatch sendCdl;
    private NioEventLoopGroup group;

    public SendFileClient(SendFileJobDescription jobDescription, CountDownLatch sendCdl, NioEventLoopGroup group) {
        this.jobDescription = jobDescription;
        this.sendCdl = sendCdl;
        this.group = group;
        initSendDataService();
    }

    private void initSendDataService(){
        File file = new File(jobDescription.getFilePath() + jobDescription.getFileName());
        String remoteFileName = Util.genePartName(jobDescription.getFileName());
        String remoteFilePath;
        remoteFilePath = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME).getValue("service.part_save_path");

        String[] hosts = new String[] {jobDescription.getHost()};
        this.sendService = new SendDataService(file, jobDescription.getLocalSendPos(), jobDescription.getRemoteSendPos(),
                remoteFileName, remoteFilePath, hosts, sendCdl, group);
    }

    public void start() throws Exception{
        sendService.call();
    }
}
