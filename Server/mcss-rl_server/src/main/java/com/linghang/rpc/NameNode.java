package com.linghang.rpc;


import com.linghang.dao.DBConnection;
import com.linghang.dao.UploadFileManageable;
import com.linghang.dao.impl.UploadFileManageImpl;
import com.linghang.pojo.*;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class NameNode {

    private HashMap<Integer, String> slaves;
    private UploadFileManageable uploadFileService;

    public NameNode() {
        initSlaves();
        uploadFileService = new UploadFileManageImpl();
    }

    private void initSlaves(){

        // TODO: 随机确定冗余数据接收结点，剩下的接收正常数据（负载均衡）
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
    public void sendData(String filePath, String fileName, int[] alpha) {

        // 检查文件是否已经上传过
        UploadFileManageable uploadFileService = new UploadFileManageImpl();
        if (uploadFileService.checkFileUploadedByStatus(fileName, ConstantUtil.UPLOADED) > 0){
            System.err.println("======== " + fileName + " HAS ALREADY BEEN UPLOADED ========");
            return;
        }

        CountDownLatch sendCdl = new CountDownLatch(slaves.size());
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        Runnable[] jobs = createJobs(filePath, fileName, alpha, sendCdl, group);

        long startTime = System.currentTimeMillis();

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

        long endTime = System.currentTimeMillis();
        System.err.println("******** SEND DATA RUN " + (endTime - startTime) + " ms ********");
        System.out.println("======== ALL SENDING FINISH FINISH ========");
        group.shutdownGracefully();

        // 记录上传信息
        File file = new File(filePath, fileName);
        long length = Util.getColSize(file.length()) * 3;
        String uploadFileName = Util.getFileUploadName(fileName);
        String subfix = Util.getFileSubfix(fileName);
        uploadFileService.insertUploadFile(new UploadFile(uploadFileName, subfix, length, 0));

        // 记录alpha
        for (int i = 0; i < slaves.size(); i++){
            uploadFileService.insertUploadDetail(new UploadDetail(uploadFileName, i, slaves.get(i)));
            saveAlpha(slaves.get(i), uploadFileName, alpha[i]);
        }

        // 记录冗余接收结点
        String redundancyRecvHost = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME).getValue("host.slave4");
        uploadFileService.insertUploadDetail(new UploadDetail(Util.getFileUploadName(fileName), 3, redundancyRecvHost));
        int alphaSum = alpha[0] + alpha[1] + alpha[2];
        saveAlpha(redundancyRecvHost, uploadFileName, alphaSum);
//
//        // 进行RS计算
//        DataNode dataNode = new DataNode();
//        String[] hosts = {slaves.get(0), slaves.get(1), slaves.get(2)};
//        dataNode.doRSCalc(fileName, redundancyRecvHost, hosts);
//
//        // 进行Lag插值计算
//        int[] x = uploadFileService.getXValues();
//        String[] lagCalcHosts = {slaves.get(0), slaves.get(1), slaves.get(1), redundancyRecvHost};
//        dataNode.doLagEncode(fileName, x, alpha, lagCalcHosts);
    }

    /**
     * 创建可执行任务列表
     * @param filePath 需要上传的文件的路径
     * @param fileName 需要上传的文件的文件名
     * @return 可执行任务列表
     */
    private Runnable[] createJobs(String filePath, String fileName, int[] alpha, CountDownLatch sendCdl, NioEventLoopGroup group){
        SendFileJobFactory sendFileJobFactory = new SendFileJobFactory(filePath, fileName, slaves, sendCdl, group);
        Runnable[] jobs = new Runnable[slaves.size()];

        for (int i = 0; i < slaves.size(); i++){
            sendFileJobFactory.setBlockIdx(i);
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

    private int saveAlpha(String host, String fileName, int alpha){
        PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
        String driver = propertiesUtil.getValue("db.driver");
        String username = propertiesUtil.getValue("db.username");
        String password = propertiesUtil.getValue("db.slave.password");
        String url = "jdbc:mysql://" + host + ":3306/dsz";
        DBConnection dbConnection = new DBConnection(driver, username, password, url);
        Connection conn = dbConnection.getConnection();
        if (conn != null){
            String sql = "insert into alpha_map (filename,alpha) values (?,?)";
            try {
                PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(sql);
                pstmt.setString(1, fileName);
                pstmt.setInt(2, alpha);
                int i = pstmt.executeUpdate();
                pstmt.close();
                return i;

            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return -1;
    }
}
