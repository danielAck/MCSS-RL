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
        NameNode nameNode = new NameNode();
        nameNode.sendDataTest(filePath, fileName);
    }

    public void clusterTest(){
        String filePath = "F:\\WUST\\program\\dsz\\";
        String fileName = "1M.pdf";
        NameNode nameNode = new NameNode();
        nameNode.sendData(filePath, fileName);
    }
}
