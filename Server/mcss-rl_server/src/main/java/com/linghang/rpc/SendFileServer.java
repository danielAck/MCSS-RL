package com.linghang.rpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.net.InetSocketAddress;
import java.util.Date;

public class SendFileServer {

    private int port;

    public SendFileServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int port = 9999;
        SendFileServer server = new SendFileServer(port);
        server.start();
    }

    public void start() throws Exception{
        NioEventLoopGroup group = new NioEventLoopGroup();
        try{
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new ObjectEncoder())
                                    .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers
                                            .weakCachingConcurrentResolver(null)))
                                    .addLast(new ServerReceiveHandler(true));
                        }
                    });
            ChannelFuture f = b.bind().sync();
            System.out.println("Bind finished... " + new Date());
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }

    }
}
