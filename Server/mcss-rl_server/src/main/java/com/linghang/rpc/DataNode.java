package com.linghang.rpc;

public class DataNode implements Call {

    @Override
    public void call() {
        System.out.println("DataNode make the call");
    }
}
