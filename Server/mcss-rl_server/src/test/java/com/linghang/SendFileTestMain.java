package com.linghang;

import com.linghang.rpc.NameNode;

public class SendFileTestMain {

    public static void main(String[] args) {
        SendFileTestMain testMain = new SendFileTestMain();
        testMain.clusterTest();
    }

    public void standaloneTest(){
        String filePath = "F:\\WUST\\program\\dsz\\";
        String fileName = "1M.pdf";
        NameNode nameNode = new NameNode(true);
        nameNode.sendData(filePath, fileName, true);
    }

    public void clusterTest(){
        String filePath = "F:\\WUST\\program\\dsz\\";
        String fileName = "1M.pdf";
        NameNode nameNode = new NameNode(false);
        nameNode.sendData(filePath, fileName, false);
    }
}
