package com.linghang.rpc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class RPCTest {

    public static void main(String[] args) {
        try {
            FileInputStream fis = new FileInputStream("");
            FileChannel fileChannel = fis.getChannel();
            SocketChannel socketChannel = SocketChannel.open();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
