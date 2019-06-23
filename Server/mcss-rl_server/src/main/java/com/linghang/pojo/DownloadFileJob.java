package com.linghang.pojo;

import com.linghang.service.DownLoadService;
import com.linghang.service.LagCalcService;
import com.linghang.service.Service;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;

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
            int[] x = {1, 2, -3};
            int[] alpha = {-6, 5, 4};
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
