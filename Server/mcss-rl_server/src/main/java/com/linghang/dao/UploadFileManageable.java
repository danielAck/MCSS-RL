package com.linghang.dao;

import com.linghang.pojo.CloudFile;
import com.linghang.pojo.UploadDetail;
import com.linghang.pojo.UploadFile;

import java.util.ArrayList;

public interface UploadFileManageable {

    Integer insertUploadFile(UploadFile file);

    Integer insertUploadDetail(UploadDetail detail);

    ArrayList<CloudFile> getAllCloudRecords();

    ArrayList<CloudFile> getUploadedRecords();

    UploadFile getUploadFileByFileName(String fileName);

    ArrayList<UploadDetail> getUploadDetailByFileName(String fileName);

    UploadDetail getUploadDetailByFileNameAndCloudId(String fileName, Integer cloudId);

    Integer getCloudIdByFileNameAndHost(String fileName, String host);

    String getRedundantHostByFileName(String fileName);

    int[] getXValues();

    Integer checkFileUploadedByStatus(String fileName, Integer status);

    Integer checkXValueSet();

    Integer deleteUploadFileByFileName(String fileName);

    Integer deleteUploadDetailByFileName(String fileName);

    Integer deleteUploadDetailByFileNameAndCloudId(String fileName, Integer cloudId);

    void closeConn();

}
