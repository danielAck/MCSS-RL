package com.linghang.rpc;

import com.linghang.service.RSCalcServiceFactory;
import com.linghang.service.Service;

public class DataNode {

    public static void main(String[] args) {
        String[] calcHost = new String[]{"127.0.0.1"};
        DataNode datanode = new DataNode();
        datanode.doRSCalc("1M.pdf", calcHost);
    }

    public DataNode() {
    }

    public void doRSCalc(String fileName, String[] hosts){
        // TODO: 先从数据库中查询是否已经上传

        String redundantBlockRecvHost = "127.0.0.1";
        RSCalcServiceFactory factory = new RSCalcServiceFactory(fileName, hosts, redundantBlockRecvHost);
        Service service = factory.createService();
        service.call();
    }

    public void doLagCalc(){

    }
}
