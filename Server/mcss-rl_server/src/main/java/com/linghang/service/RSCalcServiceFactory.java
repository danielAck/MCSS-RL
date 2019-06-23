package com.linghang.service;

import com.linghang.rpc.RSCalcServiceProxy;

import java.lang.reflect.Proxy;

public class RSCalcServiceFactory implements ServiceFactory {

    private String calcFileName;
    private String calcFilePath;
    private String redundancySaveFileName;
    private String redundancySaveFilePath;
    private String[] hosts;
    private String redundantBlockRecvHost;
    private boolean isDownLoad;

    public RSCalcServiceFactory(String calcFileName, String calcFilePath, String redundancySaveFileName, String redundancySaveFilePath,
                                String[] hosts, String redundantBlockRecvHost, boolean isDownLoad) {
        this.calcFileName = calcFileName;
        this.calcFilePath = calcFilePath;
        this.redundancySaveFileName = redundancySaveFileName;
        this.redundancySaveFilePath = redundancySaveFilePath;
        this.hosts = hosts;
        this.redundantBlockRecvHost = redundantBlockRecvHost;
        this.isDownLoad = isDownLoad;
    }

    @Override
    public Service createService() {

        RSCalcServiceProxy proxy = new RSCalcServiceProxy(calcFileName, calcFilePath, redundancySaveFileName, redundancySaveFilePath,
                hosts, redundantBlockRecvHost, isDownLoad);
        return (Service)Proxy.newProxyInstance(Service.class.getClassLoader(),
                new Class[]{Service.class},
                proxy);
    }
}
