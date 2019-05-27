package com.linghang.service;

import com.linghang.rpc.RSCalcServiceProxy;

import java.lang.reflect.Proxy;

public class RSCalcServiceFactory implements ServiceFactory {

    private String remoteFileName;
    private String remoteFilePath;
    private String[] hosts;
    private String redundantBlockRecvHost;

    public RSCalcServiceFactory(String remoteFileName, String remoteFilePath, String[] hosts, String redundantBlockRecvHost) {
        this.remoteFileName = remoteFileName;
        this.remoteFilePath = remoteFilePath;
        this.hosts = hosts;
        this.redundantBlockRecvHost = redundantBlockRecvHost;
    }

    @Override
    public Service createService() {

        RSCalcServiceProxy proxy = new RSCalcServiceProxy(remoteFileName, remoteFilePath, hosts, redundantBlockRecvHost);
        return (Service)Proxy.newProxyInstance(Service.class.getClassLoader(),
                new Class[]{Service.class},
                proxy);
    }
}
