package com.linghang.MultiThreadTest;

import com.linghang.rpc.server.RSCalcRPCServer;

public class RSCalcRPCServerMain {

    public static void main(String[] args) {
        // 开启 RPC 服务
        RSCalcRPCServer rsCalcRPCServer = new RSCalcRPCServer();
        try {
            rsCalcRPCServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
