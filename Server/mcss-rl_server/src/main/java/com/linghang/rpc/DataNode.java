package com.linghang.rpc;

import com.linghang.service.LagCalcService;
import com.linghang.service.RSCalcServiceFactory;
import com.linghang.service.Service;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;

public class DataNode {

    public static void main(String[] args) {
        String[] calcHosts = new String[]{"192.168.31.120", "192.168.31.121", "192.168.31.122"};
        String[] lagCalcHosts = new String[]{"127.0.0.1"};
        String redundantBlockRecvHost = "192.168.31.123";
        DataNode datanode = new DataNode();
        int[] x = new int[]{1, 2, -3};
        int[] alpha = new int[]{-6, 5, 4};
        datanode.doRSCalc("1M.pdf", redundantBlockRecvHost, calcHosts);
//        datanode.doLagDecode("1M.pdf", x, alpha, lagCalcHosts);
    }

    public DataNode() {
    }

    public void doRSCalc(String fileName, String redundantBlockRecvHost, String[] hosts){
        // TODO: 先从数据库中查询是否已经上传

        String remoteFileName = Util.genePartName(fileName);
        String remoteFilePath = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME).getValue("service.part_save_path");
        RSCalcServiceFactory factory = new RSCalcServiceFactory(remoteFileName, remoteFilePath, hosts, redundantBlockRecvHost);
        Service service = factory.createService();
        service.call();
    }

    public void doLagEncode(String fileName, int[] x, int[] alpha, String[] hosts){
        Service lagService = new LagCalcService(fileName, hosts, x, alpha, true);
        lagService.call();
    }

    public void doLagDecode(String fileName, int[] x, int[] alpha, String[] hosts){
        Service lagService = new LagCalcService(fileName, hosts, x, alpha, false);
        lagService.call();
    }
}
