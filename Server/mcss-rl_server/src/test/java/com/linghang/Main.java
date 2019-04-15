package com.linghang;

import com.linghang.rpc.NameNode;
import com.linghang.util.Util;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        Main testMain = new Main();
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
