package com.linghang.rpc.server;

import com.linghang.proto.Block;
import com.linghang.proto.RSCalcRequestHeader;
import com.linghang.proto.RedundancyBlockHeader;
import com.linghang.rpc.client.handler.ClientRSCalcHandler;
import com.linghang.rpc.client.handler.SendRedundantBlockHandler;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;
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
import java.util.concurrent.CountDownLatch;

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

    private class RSCalcRPCServerHandler extends ChannelInboundHandlerAdapter{
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

            // receive RS calculation request header
            if (msg instanceof RSCalcRequestHeader){
                RSCalcRequestHeader questHeader = (RSCalcRequestHeader) msg;
                String fileName = questHeader.getFileName();

                System.out.println("======== RPC SERVER RECEIVE RPC CALL FOR FILE : " + fileName + " ========");

                String[] slaves = new String[1];
                PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
                slaves[0] = "127.0.0.1";
//                slaves[1] = "192.168.31.120";

                String redundantBlockRecvHost = "127.0.0.1";

                // start send redundancy job
                CountDownLatch sendRedundancyCdl = new CountDownLatch(ConstantUtil.SLAVE_CNT);
                RedundancyBlockHeader header = new RedundancyBlockHeader(questHeader.getFileName(), questHeader.getStartPos());
                Thread t = new Thread(new SendRedundantBlockJob(redundantBlockRecvHost, sendRedundancyCdl, header));
                t.start();

                // start RS calculation request client
                for (String calcHost : slaves){
                    // 创建的文件传输客户端与当前rpc服务端共用同一个I/O线程
                    // TODO：当调用当前服务端多个文件的传输服务时同一个IO线程需要为2*rpc连接数个连接服务，响应变慢？
                    // TODO: 随机确定冗余块接收主机

                    System.out.println("======== RPC SERVER START RS CALC REQUEST CLIENT TO " + calcHost +"========");
                    createRSCalcQuestClient(questHeader, calcHost, sendRedundancyCdl, ctx);
                }
            }
            else{
                System.err.println("======== RECEIVE WRONG DATA TYPE ========");
                ctx.close();
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
         * @param host 发送计算所需相关块的主机IP
         * @param sendRedundancyCdl 发送冗余计算结果线程所依赖的CountDownLatch
         * @param rpcCtx rpc连接
         */
        private void createRSCalcQuestClient(final RSCalcRequestHeader questHeader, final String host, final CountDownLatch sendRedundancyCdl, final ChannelHandlerContext rpcCtx){
            Bootstrap b = new Bootstrap();
            b.group(rpcCtx.channel().eventLoop())
                    .channel(NioSocketChannel.class)
                    .remoteAddress(host, ConstantUtil.RS_CALC_SERVICE_PORT)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers
                                            .weakCachingConcurrentResolver(null)))
                                    .addLast(new ObjectEncoder())
                                    .addLast(new ClientRSCalcHandler(questHeader, sendRedundancyCdl, rpcCtx));
                        }
                    });
            b.connect();
        }

        private class SendRedundantBlockJob implements Runnable{

            private String host;
            private CountDownLatch countDownLatch;
            private RedundancyBlockHeader header;

            public SendRedundantBlockJob(String host, CountDownLatch countDownLatch, RedundancyBlockHeader header) {
                this.host = host;
                this.countDownLatch = countDownLatch;
                this.header = header;
            }

            @Override
            public void run() {
                try {
                    this.countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // calc job finish, send redundant file block
                System.out.println("======== RS CALC CLIENT BEGIN SENDING REDUNDANT FILE BLOCK TO : " +
                        host + " ========");
                try {
                    startSendRedundantBlockClient(header);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private void startSendRedundantBlockClient(final RedundancyBlockHeader header) throws Exception{
                NioEventLoopGroup group = new NioEventLoopGroup(1);
                try{
                    Bootstrap b = new Bootstrap();
                    b.group(group)
                            .channel(NioSocketChannel.class)
                            .remoteAddress(host, ConstantUtil.SEND_FILE_SERVICE_PORT)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel socketChannel) throws Exception {
                                    socketChannel.pipeline()
                                            .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers
                                                    .weakCachingConcurrentResolver(null)))
                                            .addLast(new ObjectEncoder())
                                            .addLast(new SendRedundantBlockHandler(header));
                                }
                            });
                    ChannelFuture f = b.connect().sync();
                    f.channel().closeFuture().sync();
                } finally {
                    group.shutdownGracefully();
                }
            }
        }
    }
}
