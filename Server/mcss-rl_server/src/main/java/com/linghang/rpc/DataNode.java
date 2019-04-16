package com.linghang.rpc;

import com.linghang.service.RSCalcService;
import com.linghang.service.RSCalcServiceFactory;

import java.util.HashMap;

public class DataNode {

    public static void main(String[] args) {

        DataNode datanode = new DataNode();
        HashMap<Integer, String> slaves = new HashMap<>();
        slaves.put(0, "127.0.0.1");
        datanode.doRSCalc(slaves, "1M.pdf");
    }

    public DataNode() {
    }

    public void doRSCalc(HashMap<Integer, String> slaves, String fileName){

        // TODO: 先从数据库中查询是否已经上传

        RSCalcServiceFactory factory = new RSCalcServiceFactory(slaves, fileName);
        Service service = factory.createService();
        service.call();
    }

}
