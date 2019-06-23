package com.linghang.rpc;


import com.linghang.pojo.*;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class NameNode {

    private HashMap<Integer, String> slaves;

    public NameNode(boolean test) {
        initSlaves(test);
    }

    private void initSlaves(boolean test){
        slaves = new HashMap<>();
        if (test){
            slaves = new HashMap<>();
            String slave1IP = "127.0.0.1";
            slaves.put(0, slave1IP);
        } else {
            PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
            String slave1IP = propertiesUtil.getValue("host.slave1");
            String slave2IP = propertiesUtil.getValue("host.slave2");
            String slave3IP = propertiesUtil.getValue("host.slave3");
            slaves.put(0, slave1IP);
            slaves.put(1, slave2IP);
            slaves.put(2, slave3IP);
        }

    }

    /**
     * 将文件分块传输给三个云
     * @param filePath 需要上传的文件的路径
     * @param fileName 需要上传的文件的文件名
     */
    public void sendData(String filePath, String fileName, boolean test) {
        CountDownLatch sendCdl = new CountDownLatch(slaves.size());
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        Runnable[] jobs = createJobs(filePath, fileName, sendCdl, group, test);
        for (int i = 0; i < slaves.size(); i ++){
            if (jobs[i] != null){
                Thread t = new Thread(jobs[i]);
                t.setName(slaves.get(i) + "-send_job");
                t.start();
            }
        }

        // wait sending data
        try {
            sendCdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("======== ALL SENDING FINISH FINISH ========");
        group.shutdownGracefully();
    }

    /**
     * 创建可执行任务列表
     * @param filePath 需要上传的文件的路径
     * @param fileName 需要上传的文件的文件名
     * @return 可执行任务列表
     */
    private Runnable[] createJobs(String filePath, String fileName, CountDownLatch sendCdl, NioEventLoopGroup group, boolean test){
        SendFileJobFactory sendFileJobFactory = new SendFileJobFactory(filePath, fileName, slaves, sendCdl, group, test);
        Runnable[] jobs = new Runnable[slaves.size()];

        // TODO: 随机确定冗余数据接收结点，剩下的接收正常数据

        for (int i = 0; i < slaves.size(); i++){

            // TODO: 将文件块号和对应的slave编号写入数据库
            sendFileJobFactory.setBlockIdx(i);
            sendFileJobFactory.setSlaveId(i);
            jobs[i] = sendFileJobFactory.createJob();
        }
        return jobs;
    }

    /**
     * 从选择的云中下载文件
     * @param fileName 下载文件名
     * @param slaveIds 选择的云ID
     */
    public void download(String fileName, String filePath, int[] slaveIds){
        String[] hosts = new String[slaveIds.length];

        // ========== Test =========
        HashMap<Integer, String> slaves = new HashMap<>();
        slaves.put(0, "192.168.0.120");
        slaves.put(1, "192.168.0.121");
        slaves.put(2, "192.168.0.122");
        slaves.put(3, "192.168.0.123");
        // =========================

        for (int i = 0; i < slaveIds.length; i++){
            hosts[i] = slaves.get(slaveIds[i]);
        }

        Job job = new DownloadFileJob(fileName, filePath, hosts);
        job.start();
    }
}
