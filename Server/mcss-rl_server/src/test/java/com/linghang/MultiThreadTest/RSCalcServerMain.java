package com.linghang.MultiThreadTest;

import com.linghang.rpc.server.RSCalcServer;

public class RSCalcServerMain {

    public static void main(String[] args) {
        // 开启 RS计算(服务器文件发送） 服务
        RSCalcServer rsCalcServer = new RSCalcServer();
        try {
            rsCalcServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
