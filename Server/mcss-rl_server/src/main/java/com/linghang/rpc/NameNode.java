package com.linghang.rpc;


import com.linghang.NIO.Data;
import com.linghang.pojo.Job;
import com.linghang.pojo.JobFactory;
import com.linghang.pojo.SendFileJob;
import com.linghang.pojo.SendFileJobFactory;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class NameNode {

    private HashMap<Integer, String> slaves;

    public NameNode() {
        initSlaves();
    }

    private void initSlaves(){
        slaves = new HashMap<>();
        PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
        String slave1IP = propertiesUtil.getValue("host.slave1");
        String slave2IP = propertiesUtil.getValue("host.slave2");
        String slave3IP = propertiesUtil.getValue("host.slave3");
        slaves.put(0, slave1IP);
        slaves.put(1, slave2IP);
        slaves.put(2, slave3IP);
    }

    /**
     * 将文件分块传输给三个DataNode
     * @param filePath 上传文件路径
     * @param fileName 上传文件名
     */
    public void sendData(String filePath, String fileName) {
        Job[] jobs = createJobs(filePath, fileName);
        for (Job job : jobs){
            if (job != null)
                job.start();
        }
    }

    private Job[] createJobs(String filePath, String fileName){
        SendFileJobFactory sendFileJobFactory = new SendFileJobFactory(filePath, fileName, slaves);
        Job[] jobs = new Job[3];
        for (int i = 0; i < 3; i++){
            sendFileJobFactory.setBlockIdx(i);
            sendFileJobFactory.setSlaveId(i);
            jobs[i] = sendFileJobFactory.createJob();
        }
        return jobs;
    }


    // ===================  Test Function ====================

    public void sendDataTest(String filePath, String fileName){
        Job job = createJobsTest(filePath, fileName);
        if (job != null)
            job.start();
    }

    private Job createJobsTest(String filePath, String fileName){
        HashMap<Integer, String> slave = new HashMap<>();
        PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
        String slave1IP = "39.106.48.26";
        slave.put(0, slave1IP);

        SendFileJobFactory sendFileJobFactory = new SendFileJobFactory(filePath, fileName, slave);
        sendFileJobFactory.setSlaveId(0);
        sendFileJobFactory.setBlockIdx(2);

        return sendFileJobFactory.createJob();
    }
}
