package com.linghang.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class CallProxy implements InvocationHandler {

    Call call;

    public CallProxy(Call call) {
        this.call = call;
}

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Client client = new Client();

        return client.getRpcResponse(method, args);

    }

}
