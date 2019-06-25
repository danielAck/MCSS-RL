package com.linghang;

import com.linghang.rpc.NameNode;

public class SendFileTestMain {

    public static void main(String[] args) {
        SendFileTestMain testMain = new SendFileTestMain();
        testMain.clusterTest();
    }

    public void clusterTest(){
        String filePath = "F:\\WUST\\program\\dsz\\";
        String fileName = "20x.pdf";
        int[] alpha = {-6, 5, 4};
        NameNode nameNode = new NameNode(false);
        nameNode.sendData(filePath, fileName, alpha, false);
    }
}
