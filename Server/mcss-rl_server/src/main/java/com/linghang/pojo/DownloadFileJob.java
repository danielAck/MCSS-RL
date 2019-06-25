package com.linghang.pojo;

import com.linghang.dao.DBConnection;
import com.linghang.dao.UploadFileManageable;
import com.linghang.dao.impl.UploadFileManageImpl;
import com.linghang.service.DownLoadService;
import com.linghang.service.LagCalcService;
import com.linghang.service.Service;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class DownloadFileJob implements Job {

    private String fileName;
    private String filePath;
    private String[] hosts;
    private CountDownLatch lagCalcCdl;
    private PropertiesUtil propertiesUtil;

    public DownloadFileJob(String fileName, String filePath, String[] hosts) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.hosts = hosts;
        this.lagCalcCdl = new CountDownLatch(1);
        this.propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
    }

    @Override
    public void start() {
        Thread t = new Thread(new DownLoadJobExecutor());
        t.setName(fileName + "-download_job");
        t.start();
    }

    private class DownLoadJobExecutor implements Runnable{

        @Override
        public void run() {

            // TODO: 从数据库中获取 x 和 alpha
            UploadFileManageable uploadFileService = new UploadFileManageImpl();
            int[] x = uploadFileService.getXValues();
            int[] alpha = getAlphaValues(hosts, Util.getFileUploadName(fileName));
            Service lagCalcService = new LagCalcService(fileName, hosts, x, alpha, lagCalcCdl, false);
            lagCalcService.call();
            try {
                lagCalcCdl.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String localHost = propertiesUtil.getValue("host.local");
            Service downloadService = new DownLoadService(fileName, filePath, hosts, localHost);
            downloadService.call();

        }

        private int[] getAlphaValues(String[] hosts, String fileName){

            int[] alpha = new int[3];
            int[] check = new int[3];
            ArrayList<String> checkedList = new ArrayList<>(Arrays.asList(hosts));
            String redundantHost = getRedundantHost(fileName);
            checkedList.remove(redundantHost);

            for (String host : checkedList){
                UploadFileManageable uploadFileService = new UploadFileManageImpl();
                int cloudId = uploadFileService.getCloudIdByFileNameAndHost(fileName, host);
                Integer res = getAlphaValue(host, fileName);
                if (res != null) {
                    alpha[cloudId] = res;
                    check[cloudId] = 1;
                }
                else{
                    System.err.println("======== ERROR OCCURS WHILE SELECTING ALPHA VALUE IN HOST : " + host + " ========");
                    return null;
                }
            }
            if (checkedList.size() == 2){
                Integer value = getAlphaValue(redundantHost, fileName);
                if (value != null){
                    int temp = 0;
                    int idx = -1;
                    for (int i = 0; i < 3; i++){
                        if (check[i] == 0){
                            idx = i;
                        } else {
                            temp += alpha[i];
                        }
                    }
                    alpha[idx] = value - temp;
                } else {
                    System.err.println("======== ERROR OCCURS WHILE SELECTING ALPHA VALUE IN HOST : " + redundantHost + " ========");
                    return null;
                }
            }
            return alpha;
        }

        private Integer getAlphaValue(String host, String fileName){
            PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
            String driver = propertiesUtil.getValue("db.driver");
            String username = propertiesUtil.getValue("db.username");
            String password = propertiesUtil.getValue("db.slave.password");

            String url = "jdbc:mysql://" + host + ":3306/dsz";
            DBConnection dbConnection = new DBConnection(driver, username, password, url);
            Connection conn = dbConnection.getConnection();

            String sql = "select alpha from alpha_map where filename = ?";
            try {
                PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
                pstmt.setString(1, fileName);
                ResultSet rs = pstmt.executeQuery();
                rs.next();
                return rs.getInt(1);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        private String getRedundantHost(String fileName){
            UploadFileManageable uploadFileService = new UploadFileManageImpl();
            return uploadFileService.getRedundantHostByFileName(fileName);
        }

    }
}
