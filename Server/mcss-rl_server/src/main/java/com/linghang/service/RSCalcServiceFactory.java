package com.linghang.service;

import com.linghang.rpc.RSCalcServiceProxy;

import java.lang.reflect.Proxy;

public class RSCalcServiceFactory implements ServiceFactory {

    private String fileName;
    private String[] hosts;
    private String redundantBlockRecvHost;

    public RSCalcServiceFactory(String fileName, String[] hosts, String redundantBlockRecvHost) {
        this.fileName = fileName;
        this.hosts = hosts;
        this.redundantBlockRecvHost = redundantBlockRecvHost;
    }

    @Override
    public Service createService() {

        RSCalcServiceProxy proxy = new RSCalcServiceProxy(fileName, hosts, redundantBlockRecvHost);
        return (Service)Proxy.newProxyInstance(Service.class.getClassLoader(),
                new Class[]{Service.class},
                proxy);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
