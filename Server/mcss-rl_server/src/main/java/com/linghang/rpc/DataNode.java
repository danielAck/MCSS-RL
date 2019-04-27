package com.linghang.rpc;

import com.linghang.service.RSCalcServiceFactory;
import com.linghang.service.Service;

public class DataNode {

    public static void main(String[] args) {
        DataNode datanode = new DataNode();
        datanode.doRSCalc("1M.pdf");
    }

    public DataNode() {
    }

    public void doRSCalc(String fileName){

        // TODO: 先从数据库中查询是否已经上传

        RSCalcServiceFactory factory = new RSCalcServiceFactory(fileName);
        Service service = factory.createService();
        service.call();
    }
}
