package com.linghang.pojo;

import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;

import java.io.File;
import java.util.HashMap;

public class SendFileJobFactory implements JobFactory {

    private String filePath;
    private String fileName;
    private HashMap<Integer, String> slaves;
    private Integer blockIdx;
    private Integer slaveId;

    public SendFileJobFactory(String filePath, String fileName,
                              HashMap<Integer, String> slaves) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.slaves = slaves;
    }

    @Override
    public Job createJob() {

        if (blockIdx == null || slaveId == null){
            System.err.println("======= PLEASE SET blockIdx & slaveId FIRST ! =======");
            return null;
        }

        // check if file exists
        boolean fileExist = checkFileExist();
        if (!fileExist)
            return null;

        // get slave IP
        PropertiesUtil serverProperties = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
        String slaveIP = slaves.get(slaveId);
        if (slaveIP == null){
            System.err.println("======= SLAVE ID:" + slaveId + " DOESN'T EXIST !");
            return null;
        }

        // get service corresponds port
        String serviceName = "service.sendfile";
        String servicePortValue = serverProperties.getValue(serviceName);
        if (servicePortValue == null){
            System.err.println("======= SERVICE: " + serviceName + " DOESN'T EXIST IN PROPERTY : " + ConstantUtil.SERVER_PROPERTY_NAME);
            return null;
        }
        int servicePort = Integer.parseInt(servicePortValue);

        // get send position
        SendPosition sendPosition = getSendPosition();

        return new SendFileJob(filePath, fileName, sendPosition, slaveIP, servicePort);

    }

    private boolean checkFileExist(){
        File file  = new File(filePath + fileName);
        if (!file.exists()){
            System.err.println("======= ERROR : SENDING FILE DOESN'T EXIST ! ========");
            return false;
        }
        return true;
    }

    private SendPosition getSendPosition(){
        File file = new File(filePath + fileName);
        long col_size = Util.getColSize(file.length());
        long startPos = col_size * blockIdx;
        long endPos = startPos + col_size - 1;
        return new SendPosition(startPos, endPos);
    }

    public Integer getBlockIdx() {
        return blockIdx;
    }

    public void setBlockIdx(Integer blockIdx) {
        this.blockIdx = blockIdx;
    }

    public Integer getSlaveId() {
        return slaveId;
    }

    public void setSlaveId(Integer slaveId) {
        this.slaveId = slaveId;
    }
}
