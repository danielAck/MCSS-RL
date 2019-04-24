package com.linghang.rpc.server;

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
        RSCalcServer calcService = new RSCalcServer();

        // expose service
        try {
            calcService.start();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
