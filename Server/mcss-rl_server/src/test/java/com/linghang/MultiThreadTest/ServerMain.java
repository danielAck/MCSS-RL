package com.linghang.MultiThreadTest;

import com.linghang.rpc.server.RSCalcRPCServer;
import com.linghang.rpc.server.RSCalcServer;

public class ServerMain {

    public static void main(String[] args) {
        // 开启 RPC 服务
        RSCalcRPCServer rsCalcRPCServer = new RSCalcRPCServer();
        // 开启 RS计算(服务器文件发送） 服务
        RSCalcServer rsCalcServer = new RSCalcServer();
        try {
            rsCalcServer.start();
            rsCalcRPCServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
