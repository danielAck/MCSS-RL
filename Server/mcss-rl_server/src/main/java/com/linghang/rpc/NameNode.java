package com.linghang.rpc;


import com.linghang.NIO.Data;
import com.linghang.pojo.Job;
import com.linghang.pojo.JobFactory;
import com.linghang.pojo.SendFileJob;
import com.linghang.pojo.SendFileJobFactory;
import com.linghang.service.DownLoadService;
import com.linghang.service.Service;
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
     * 将文件分块传输给三个云
     * @param filePath 需要上传的文件的路径
     * @param fileName 需要上传的文件的文件名
     */
    public void sendData(String filePath, String fileName) {
        Job[] jobs = createJobs(filePath, fileName);
        for (Job job : jobs){
            if (job != null)
                job.start();
        }
    }

    /**
     * 从选择的云中下载文件
     * @param fileName 下载文件名
     * @param slaveIds 选择的云ID
     */
    public void download(String fileName, int[] slaveIds){
        String[] hosts = new String[slaveIds.length];

        // ========== Test =========
        HashMap<Integer, String> slaves = new HashMap<Integer,String>();
        slaves.put(0, "127.0.0.1");
        // =========================

        for (int i = 0; i < slaveIds.length; i++){
            hosts[i] = slaves.get(i);
        }
        Service downloadService = new DownLoadService(fileName, hosts);
        downloadService.call();
    }

    /**
     * 创建可执行任务列表
     * @param filePath 需要上传的文件的路径
     * @param fileName 需要上传的文件的文件名
     * @return 可执行任务列表
     */
    private Job[] createJobs(String filePath, String fileName){
        SendFileJobFactory sendFileJobFactory = new SendFileJobFactory(filePath, fileName, slaves);
        Job[] jobs = new Job[3];

        // TODO: 随机确定冗余数据接收结点，剩下的接收正常数据

        for (int i = 0; i < 3; i++){

            // TODO: 将文件块号和对应的slave编号写入数据库
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
        String slave1IP = "127.0.0.1";
        slave.put(0, slave1IP);

        SendFileJobFactory sendFileJobFactory = new SendFileJobFactory(filePath, fileName, slave);
        sendFileJobFactory.setSlaveId(0);
        sendFileJobFactory.setBlockIdx(2);

        return sendFileJobFactory.createJob();
    }
}
