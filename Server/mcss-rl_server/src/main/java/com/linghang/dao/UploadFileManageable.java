package com.linghang.dao;

import com.linghang.pojo.UploadDetail;
import com.linghang.pojo.UploadFile;

import java.util.ArrayList;

public interface UploadFileManageable {

    Integer insertUploadFile(UploadFile file);

    Integer insertUploadDetail(UploadDetail detail);

    ArrayList<UploadFile> getAllUploadFile();

    UploadFile getUploadFileByFileName(String fileName);

    ArrayList<UploadDetail> getUploadDetailByFileName(String fileName);

    UploadDetail getUploadDetailByFileNameAndCloudId(String fileName, Integer cloudId);

    Integer getCloudIdByFileNameAndHost(String fileName, String host);

    String getRedundantHostByFileName(String fileName);

    Integer checkFileUploaded(String fileName);

    Integer deleteUploadFileByFileName(String fileName);

    Integer deleteUploadDetailByFileName(String fileName);

    Integer deleteUploadDetailByFileNameAndCloudId(String fileName, Integer cloudId);

}
