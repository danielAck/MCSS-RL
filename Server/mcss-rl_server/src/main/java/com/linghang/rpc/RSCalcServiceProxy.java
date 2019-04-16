package com.linghang.rpc;

import com.linghang.service.RSCalcService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;

public class RSCalcServiceProxy implements InvocationHandler {

    private String fileName;
    private HashMap<Integer, String> slaves;

    public RSCalcServiceProxy(HashMap<Integer, String> slaves, String fileName) {
        this.slaves = slaves;
        this.fileName = fileName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        boolean res = true;

        // 依次进行远程调用


        return res;
    }


}
