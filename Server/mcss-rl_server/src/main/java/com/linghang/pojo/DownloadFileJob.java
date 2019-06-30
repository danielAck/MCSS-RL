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

            UploadFileManageable uploadFileService = new UploadFileManageImpl();
            int[] x = uploadFileService.getXValues();
            int[] alpha = Util.getAlphaValues(hosts, Util.getFileUploadName(fileName));
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

    }
}
