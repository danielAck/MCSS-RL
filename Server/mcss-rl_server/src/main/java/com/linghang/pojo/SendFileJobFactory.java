package com.linghang.pojo;

import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class SendFileJobFactory implements JobFactory {

    private String localFilePath;
    private String localFileName;
    private HashMap<Integer, String> slaves;
    private Integer blockIdx;
    private CountDownLatch sendCdl;
    private NioEventLoopGroup group;

    public SendFileJobFactory(String localFilePath, String localFileName,
                              HashMap<Integer, String> slaves, CountDownLatch sendCdl, NioEventLoopGroup group) {
        this.localFilePath = localFilePath;
        this.localFileName = localFileName;
        this.slaves = slaves;
        this.group = group;
        this.sendCdl = sendCdl;
    }

    @Override
    public Runnable createJob() {

        if (blockIdx == null){
            System.err.println("======= PLEASE SET blockIdx & slaveId FIRST ! =======");
            return null;
        }

        // check if file exists
        boolean fileExist = checkFileExist();
        if (!fileExist)
            return null;

        // get slave IP
        String slaveIP = slaves.get(blockIdx);
        if (slaveIP == null){
            System.err.println("======= SLAVE ID:" + blockIdx + " DOESN'T EXIST !");
            return null;
        }

        // 获取发送文件块对应的字节下标
        SendPosition localSendPosition = getLocalSendPosition();
        SendPosition remoteSendPosition = getRemoteSendPosition();

        return new SendFileJob(localFilePath, localFileName, localSendPosition, remoteSendPosition, slaveIP, sendCdl, group);
    }

    private boolean checkFileExist(){
        File file  = new File(localFilePath + localFileName);
        if (!file.exists()){
            System.err.println("======= ERROR : LOCAL FILE THAT NEED SEND DOESN'T EXIST ! ========");
            return false;
        }
        return true;
    }

    private SendPosition getLocalSendPosition(){
        File file = new File(localFilePath + localFileName);
        long col_size = Util.getColSize(file.length());
        long startPos = col_size * blockIdx;
        long endPos = startPos + col_size;
        return new SendPosition(startPos, endPos);
    }

    private SendPosition getRemoteSendPosition(){
        return new SendPosition(0, -1);
    }

    public Integer getBlockIdx() {
        return blockIdx;
    }

    public void setBlockIdx(Integer blockIdx) {
        this.blockIdx = blockIdx;
    }

}
