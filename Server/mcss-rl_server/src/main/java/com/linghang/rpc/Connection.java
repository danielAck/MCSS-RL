package com.linghang.rpc;

import java.net.InetAddress;

public class Connection {

    private int port;
    private InetAddress address;

    public Connection (InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public void getConnection() {
        System.out.println("Get Connection with ip : " + address.toString() + " in port :" + port);
    }

}
