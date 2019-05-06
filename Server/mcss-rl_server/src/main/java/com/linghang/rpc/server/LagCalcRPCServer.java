package com.linghang.rpc.server;

import com.linghang.proto.LagCalcRequestHeader;
import com.linghang.service.LagCalcService;
import com.linghang.service.Service;
import com.linghang.util.ConstantUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class LagCalcRPCServer {

    public void start(){

        NioEventLoopGroup group = new NioEventLoopGroup(1);
        ServerBootstrap b = new ServerBootstrap();
        b.group(group)
                .channel(NioServerSocketChannel.class)
                .localAddress(ConstantUtil.LAG_CALC_RPC_PORT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new ObjectEncoder())
                                .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers
                                        .weakCachingConcurrentResolver(null)))
                                .addLast(new LagCalcRPCServerHandler());
                    }
                });
    }

    private class LagCalcRPCServerHandler extends ChannelInboundHandlerAdapter{

        private Service lagCalcService = null;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof LagCalcRequestHeader){
                LagCalcRequestHeader header = (LagCalcRequestHeader) msg;
                lagCalcService = new LagCalcService(header, ctx);
                System.out.println("======== SERVER RECEIVE LAG CALC REQUEST FOR FILE " + header.getFileName() + " ========");
                lagCalcService.call();
            }
            else{
                System.err.println("======== SERVER RECEIVE WRONG DATA TYPE");
                ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }

}
