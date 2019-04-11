package com.linghang.pojo;

import com.linghang.util.ConstantUtil;
import com.linghang.util.Util;

import java.io.File;

public class SendFileJobFactory implements JobFactory {

    private String filePath;
    private String fileName;

    public SendFileJobFactory(String filePath, String fileName) {
        this.filePath = filePath;
        this.fileName = fileName;
    }

    @Override
    public Job generateJob() {

        return new SendFileJob(filePath, fileName);

    }

}
