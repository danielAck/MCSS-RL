package com.linghang.service;

import com.linghang.rpc.Service;
import com.linghang.rpc.RSCalcServiceProxy;

import java.lang.reflect.Proxy;
import java.util.HashMap;

public class RSCalcServiceFactory implements ServiceFactory {

    private HashMap<Integer, String> slaves;
    private String fileName;

    public RSCalcServiceFactory(HashMap<Integer, String> slaves, String fileName) {
        this.slaves = slaves;
        this.fileName = fileName;
    }

    @Override
    public Service createService() {

        RSCalcServiceProxy proxy = new RSCalcServiceProxy(slaves, fileName);
        return (Service)Proxy.newProxyInstance(Service.class.getClassLoader(),
                new Class[]{Service.class},
                proxy);
    }

    public void setHost(String fileName) {
        this.fileName = fileName;
    }
}
