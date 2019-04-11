package com.linghang.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.Date;

public class EchoClient {

    private final String host;
    private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)  // 对引导指定处理
                    .channel(NioSocketChannel.class)    // 对处理的Channel指定为NIO
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoClientHandler());
                        }
                    });
            ChannelFuture f = b.connect().sync(); // 同步阻塞至连接完成
            System.out.println("Client connect finished...." + new Date());
            f.channel().closeFuture().sync(); // 同步阻塞至Channel关闭
            System.out.println("Client closed...." + new Date());
        } finally {
            group.shutdownGracefully().sync(); // 同步阻塞至线程池关闭并且释放所有资源
        }
    }

    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 9999;
        new EchoClient(host, port).start();
    }
}
