package com.linghang.rpc;


import com.linghang.NIO.Data;
import com.linghang.pojo.Job;
import com.linghang.pojo.JobFactory;
import com.linghang.pojo.SendFileJob;
import com.linghang.pojo.SendFileJobFactory;
import com.linghang.util.Util;

import java.io.File;
import java.io.IOException;

public class NameNode {

    /**
     * 将文件分块传输给三个DataNode
     * @param filePath 上传文件路径
     * @param fileName 上传文件名
     */
    public void sendData(String filePath, String fileName) {

        File file  = new File(filePath + fileName);
        if (!file.exists()){
            System.err.println("======= ERROR : SENDING FILE DOESN'T EXIST ! ========");
            return;
        }

        startJobs(file, filePath, fileName);
    }

    private void startJobs(File file, String filePath, String fileName){

        int colSize = Util.getColSize(file.length());
        String host1 = "";
        String host2 = "";
        String host3 = "";
        int port = 9999;
        Job host1_job = new SendFileJob(filePath, fileName, 1,0, colSize-1, host1, port);
        Job host2_job = new SendFileJob(filePath, fileName, 2, colSize, colSize*2-1, host2, port);
        Job host3_job = new SendFileJob(filePath, fileName, 3, colSize*2, colSize*3-1, host3, port);
        host1_job.start();
    }

}
