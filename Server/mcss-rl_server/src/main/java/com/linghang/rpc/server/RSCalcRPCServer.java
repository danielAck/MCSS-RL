package com.linghang.rpc.server;

import com.linghang.proto.BlockDetail;
import com.linghang.proto.RSCalcRequestHeader;
import com.linghang.rpc.client.SendRedundancyHandler;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class RSCalcRPCServer {


    public RSCalcRPCServer() {
    }

    public void start() throws Exception{

        NioEventLoopGroup group = new NioEventLoopGroup(1);
        ServerBootstrap b = new ServerBootstrap();
        b.group(group)
                .channel(NioServerSocketChannel.class)
                .localAddress(ConstantUtil.RS_CALC_RPC_PORT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers
                                        .weakCachingConcurrentResolver(null)))
                                .addLast(new ObjectEncoder())
                                .addLast(new RSCalcRPCServerHandler());
                    }
                });
        ChannelFuture f = b.bind().sync();
        System.out.println("======== RS CALCULATE RPC SERVER START ========");
        f.channel().closeFuture().sync();
    }

    private static class RSCalcRPCServerHandler extends ChannelInboundHandlerAdapter{
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

            // receive RS calculation request header
            if (msg instanceof RSCalcRequestHeader){
                RSCalcRequestHeader questHeader = (RSCalcRequestHeader) msg;
                String fileName = questHeader.getFileName();

                System.out.println("======== RPC SERVER RECEIVE RPC CALL FOR FILE : " + fileName + " ========");

                String[] slaves = new String[2];
                PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
                slaves[0] = "127.0.0.1";
                slaves[1] = "192.168.31.120";

                String redundantBlockRecvHost = "192.168.31.123";
                for (String calcHost : slaves){
                    // 创建的文件传输客户端与当前rpc服务端共用同一个I/O线程
                    // TODO：当调用当前服务端多个文件的传输服务时同一个IO线程需要为2*rpc连接数个连接服务，响应变慢？
                    // TODO: 随机确定冗余块接收主机

                    System.out.println("======== RPC SERVER START SEND REDUNDANCY CLIENT TO " + calcHost +"========");
                    createSendRedundancyClient(questHeader, calcHost, redundantBlockRecvHost, ctx);
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }


        /**
         * 开启请求文件块客户端，请求到数据后进行RS计算
         * @param questHeader 请求文件块信息
         * @param calcHost 发送计算所需相关块的主机IP
         * @param redundancyRecvHost 接收冗余计算结果的主机IP
         * @param rpcCtx rpc连接
         */
        private void createSendRedundancyClient(final RSCalcRequestHeader questHeader, final String calcHost, final String redundancyRecvHost, final ChannelHandlerContext rpcCtx){
            Bootstrap b = new Bootstrap();
            b.group(rpcCtx.channel().eventLoop())
                    .channel(NioSocketChannel.class)
                    .remoteAddress(redundancyRecvHost, ConstantUtil.SEND_FILE_SERVICE_PORT)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers
                                            .weakCachingConcurrentResolver(null)))
                                    .addLast(new ObjectEncoder())
                                    .addLast(new SendRedundancyHandler(questHeader, calcHost, rpcCtx));
                        }
                    });
            b.connect();
        }
    }
}
