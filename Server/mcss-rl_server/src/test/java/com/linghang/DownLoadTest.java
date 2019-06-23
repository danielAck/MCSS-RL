package com.linghang;

import com.linghang.rpc.NameNode;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;

public class DownLoadTest {

    public static void main(String[] args) {
        NameNode nameNode = new NameNode(true);
        String fileName = "1M.pdf";
        String filePath = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME).getValue("service.local_download_path");
        int[] selectedIdx = new int[]{0, 1, 3};
        nameNode.download(fileName, filePath, selectedIdx);
    }

}
