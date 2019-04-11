package com.linghang.rpc;

import java.lang.reflect.Proxy;

public class CallProxyFactory {


    public static void main(String[] args) {
        CallProxy proxy = new CallProxy(new DataNode());
        Call call = (Call) Proxy.newProxyInstance(Call.class.getClassLoader(),
                                                    new Class[]{Call.class},
                                                    proxy);
        call.call();
    }

}
