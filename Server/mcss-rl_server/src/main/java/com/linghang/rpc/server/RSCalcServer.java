package com.linghang.rpc.server;

import com.linghang.rpc.server.handler.ServerFileRequestHandler;
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

public class RSCalcServer {

    public RSCalcServer() {
    }

    // start server
    public void start() throws Exception{
        NioEventLoopGroup group = new NioEventLoopGroup();
        try{
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(ConstantUtil.RS_CALC_SERVICE_PORT)
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
            System.out.println("======== RS CALCULATE SERVER START ========");
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

}
