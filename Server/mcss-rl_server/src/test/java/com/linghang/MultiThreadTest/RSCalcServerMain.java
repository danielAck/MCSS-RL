package com.linghang.MultiThreadTest;

import com.linghang.rpc.server.GetBlockServer;

public class RSCalcServerMain {

    public static void main(String[] args) {
        // 开启 RS计算(服务器文件发送） 服务
        GetBlockServer getBlockServer = new GetBlockServer();
        try {
            getBlockServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
