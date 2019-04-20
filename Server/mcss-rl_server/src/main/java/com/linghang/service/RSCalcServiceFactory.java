package com.linghang.service;

import com.linghang.rpc.Service;
import com.linghang.rpc.RSCalcServiceProxy;

import java.lang.reflect.Proxy;

public class RSCalcServiceFactory implements ServiceFactory {

    private String fileName;

    public RSCalcServiceFactory(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Service createService() {

        RSCalcServiceProxy proxy = new RSCalcServiceProxy(fileName);
        return (Service)Proxy.newProxyInstance(Service.class.getClassLoader(),
                new Class[]{Service.class},
                proxy);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
