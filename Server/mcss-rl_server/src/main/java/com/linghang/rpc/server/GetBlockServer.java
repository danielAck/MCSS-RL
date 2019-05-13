package com.linghang.rpc.server;

import com.linghang.rpc.server.handler.GetBlockRequestHandler;
import com.linghang.util.ConstantUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class GetBlockServer {

    public GetBlockServer() {
    }

    // start server
    public void start() throws Exception{
        NioEventLoopGroup group = new NioEventLoopGroup();
        try{
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(ConstantUtil.GET_DATA_SERVICE_PORT)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers
                                            .weakCachingConcurrentResolver(null)))
                                    .addLast(new ObjectEncoder())
                                    .addLast(new GetBlockRequestHandler());
                        }
                    });
            ChannelFuture f = b.bind().sync();
            System.out.println("======== GET BLOCK SERVER START ========");
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        GetBlockServer server = new GetBlockServer();
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
