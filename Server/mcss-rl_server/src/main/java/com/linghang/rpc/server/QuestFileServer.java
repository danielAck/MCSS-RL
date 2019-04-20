package com.linghang.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.net.InetSocketAddress;

public class QuestFileServer {

    private int port;

    public QuestFileServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        QuestFileServer server = new QuestFileServer(9999);
        server.start();
    }

    public void start() throws Exception{
        EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers
                                            .weakCachingConcurrentResolver(null)))
                                    .addLast(new ObjectEncoder())
                                    .addLast(new ServerFileRequestHandler());
                        }
                    });
            ChannelFuture f = b.bind().sync();
            f.channel().closeFuture().sync();
        }
        finally {
            group.shutdownGracefully();
        }
    }


}
