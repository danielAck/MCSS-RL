package com.linghang.rpc;

import com.linghang.service.LagCalcService;
import com.linghang.service.RSCalcServiceFactory;
import com.linghang.service.Service;

public class DataNode {

    public static void main(String[] args) {
        String[] calcHosts = new String[]{"127.0.0.1"};
        DataNode datanode = new DataNode();
        datanode.doRSCalc("1M.pdf", calcHosts);
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

    public void doLagEncode(String fileName, String[] hosts){
        Service lagService = new LagCalcService(fileName, hosts, true);
        lagService.call();
    }

    public void doLagDecode(String fileName, String[] hosts){
        Service lagService = new LagCalcService(fileName, hosts, false);
        lagService.call();
    }
}
