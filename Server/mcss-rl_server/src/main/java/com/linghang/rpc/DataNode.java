package com.linghang.rpc;

import com.linghang.dao.UploadFileManageable;
import com.linghang.dao.impl.UploadFileManageImpl;
import com.linghang.service.LagCalcService;
import com.linghang.service.RSCalcServiceFactory;
import com.linghang.service.Service;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;

public class DataNode {

    public static void main(String[] args) {
        String[] RSCalcHosts = new String[]{"192.168.0.120", "192.168.0.121", "192.168.0.122"};
        String[] lagCalcHosts = new String[]{"192.168.0.120", "192.168.0.121", "192.168.0.122", "192.168.0.123"};
        String redundantBlockRecvHost = "192.168.0.123";
        DataNode datanode = new DataNode();
        int[] x = {1, 2, -3};
        int[] alpha = {-6, 5, 4};
//        datanode.doRSCalc("2M.txt", redundantBlockRecvHost, RSCalcHosts);
        datanode.doLagEncode("2M.txt", x, alpha, lagCalcHosts);
//        datanode.doLagDecode("1M.pdf", x, alpha, lagCalcHosts);
    }

    public DataNode() {
    }

    private void doRSCalc(String fileName, String redundantBlockRecvHost, String[] hosts){
        UploadFileManageable uploadFileService = new UploadFileManageImpl();
        int res = uploadFileService.checkFileUploaded(Util.getFileUploadName(fileName));
        if (res > 0){
            String calcFileName = Util.genePartName(fileName);
            String calcFilePath = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME).getValue("service.part_save_path");
            RSCalcServiceFactory factory = new RSCalcServiceFactory(calcFileName, calcFilePath, calcFileName, calcFilePath,
                    hosts, redundantBlockRecvHost, false);
            Service service = factory.createService();
            service.call();
        } else if (res == 0){
            System.err.println("======== PLEASE UPLOAD FILE FIRST ========");
        } else {
            System.err.println("======== ERROR OCCUR IN DB CONNECTION ========");
        }
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
