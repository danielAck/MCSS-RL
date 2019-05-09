package com.linghang.service;

import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;

public class DownLoadService implements Service{

    private String fileName;
    private Service rsCalcService;

    public DownLoadService(String fileName, String[] hosts) {
        this.fileName = fileName;
        rsCalcService = new RSCalcServiceFactory(fileName, hosts, getLocalHost()).createService();
    }

    @Override
    public void call() {
        // TODO： 用slaveIds从数据库中获取对应的IP

        rsCalcService.call();

    }

    private String getLocalHost(){
        PropertiesUtil util = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
        return util.getValue("host.local");
    }
}
