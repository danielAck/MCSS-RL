package com.linghang.rpc.server;

import com.linghang.service.RSCalcServerService;

public class Server {

    public Server() {}

    public void init(){
        boolean initSuccess = exposeService();
        if (!initSuccess){
            System.err.println("======== DATA NODE EXPOSE SERVICE FAILED ========");
        }
    }

    // expose server service
    private boolean exposeService(){

        // init server service
        RSCalcServerService calcService = new RSCalcServerService();

        // expose service
        try {
            calcService.expose();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
