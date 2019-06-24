package com.linghang.rpc.server;

import com.linghang.pojo.SendPosition;
import com.linghang.proto.Block;
import com.linghang.proto.GetBlockHeader;
import com.linghang.proto.RSCalcRequestHeader;
import com.linghang.proto.RedundancyBlockHeader;
import com.linghang.rpc.client.handler.ClientRSCalcHandler;
import com.linghang.rpc.client.handler.SendRedundantBlockHandler;
import com.linghang.service.SendDataService;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
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

import java.io.File;
import java.util.ArrayList;
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
                String calcFileName = questHeader.getCalcFileName();
                String calcFilePath = questHeader.getCalcFilePath();
                String redundancySaveFilePath = questHeader.getRedundancySaveFilePath();
                String redundancySaveFileName = questHeader.getRedundancySaveFileName();

                System.out.println("======== RPC SERVER RECEIVE RPC CALL FOR FILE : " + calcFileName + " ========");
                ArrayList<String> calcHosts = questHeader.getCalcHosts();
                // 获取冗余计算结果接受节点IP
                String redundantBlockRecvHost = questHeader.getRedundantBlockRecvHost();


                // start send redundancy block job
                CountDownLatch sendWaitCdl = new CountDownLatch(calcHosts.size());
                String sendFileName = Util.geneTempName(calcFileName);
                String sendFilePath = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME).getValue("service.calc_temp_save_path");
                Thread t = new Thread(new SendRedundantBlockJob(redundantBlockRecvHost, sendWaitCdl,
                        sendFileName, sendFilePath, redundancySaveFileName, redundancySaveFilePath,
                        questHeader.getBlockIdx(), questHeader.isDownLoad()));
                t.start();

                // start get block client
                GetBlockHeader getBlockHeader = createGetBlockHeader(calcFileName, calcFilePath, questHeader.getBlockIdx());
                if (getBlockHeader == null){
                    ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
                    return;
                }
                for (String calcHost : calcHosts){
                    // 创建的文件传输客户端与当前rpc服务端共用同一个I/O线程
                    // TODO：当调用当前服务端多个文件的传输服务时同一个IO线程需要为2*rpc连接数个连接服务，响应变慢？
                    // TODO: 随机确定冗余块接收主机

                    System.out.println("======== RPC SERVER START GET DATA REQUEST CLIENT TO " + calcHost +"========");
                    createGetDataClient(getBlockHeader, calcHost, sendWaitCdl, ctx);
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

        private long getRemoteSendStartPos(String sendFilePath, String sendFileName, long idx, boolean isDownload){
            File file = new File(sendFilePath + sendFileName);
            if (isDownload){
                long startPos = 2*file.length()*3 + idx*file.length();
                System.out.println("{ File :" + file.toString() + "file length = " + file.length() + " idx = " + idx + " startPos = " + startPos +" }");
                return startPos;
            } else {
                long startPos = idx*file.length();
                System.out.println("{ File :" + file.toString() + "file length = " + file.length() + " idx = " + idx + " startPos = " + startPos +" }");
                return startPos;
            }
        }

        /**
         * 开启请求文件块客户端，请求到数据后进行RS计算
         * @param getBlockHeader 请求文件块信息
         * @param host 发送计算所需相关块的主机IP
         * @param sendRedundancyCdl 发送冗余计算结果线程所依赖的CountDownLatch
         * @param rpcCtx rpc连接
         */
        private void createGetDataClient(final GetBlockHeader getBlockHeader, final String host, final CountDownLatch sendRedundancyCdl, final ChannelHandlerContext rpcCtx){
            Bootstrap b = new Bootstrap();
            b.group(rpcCtx.channel().eventLoop())
                    .channel(NioSocketChannel.class)
                    .remoteAddress(host, ConstantUtil.GET_DATA_SERVICE_PORT)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers
                                            .weakCachingConcurrentResolver(null)))
                                    .addLast(new ObjectEncoder())
                                    .addLast(new ClientRSCalcHandler(getBlockHeader, sendRedundancyCdl, rpcCtx));
                        }
                    });
            b.connect();
        }

        /**
         * 生成文件块请求头
         * @param calcFileName 本地文件块名
         * @param calcFilePath 本地文件块路径
         * @return 请求头
         */
        private GetBlockHeader createGetBlockHeader(String calcFileName, String calcFilePath, int blockIdx){
            File file = new File(calcFilePath + calcFileName);
            long blockLength = file.length() / 3;
            if (!file.exists()){
                System.err.println("======== " + calcFilePath + calcFileName + " FILE DON'T EXIST ! ========");
                return null;
            }
            long startPos = blockLength * blockIdx;

            return new GetBlockHeader(calcFileName, calcFilePath, startPos, blockLength);
        }

        private class SendRedundantBlockJob implements Runnable{

            private String host;
            private CountDownLatch sendWaitCdl;
            private CountDownLatch waitSendFinishCdl;
            private String sendFileName;
            private String sendFilePath;
            private String remoteFileName;
            private String remoteFilePath;
            private int idx;
            private boolean isDownload;
            private long remoteSendStartPos;

            public SendRedundantBlockJob(String host, CountDownLatch sendWaitCdl, String sendFileName, String sendFilePath,
                                         String remoteFileName, String remoteFilePath, int idx, boolean isDownload) {
                this.host = host;
                this.sendWaitCdl = sendWaitCdl;
                this.waitSendFinishCdl = new CountDownLatch(1);
                this.sendFileName = sendFileName;
                this.sendFilePath = sendFilePath;
                this.remoteFileName = remoteFileName;
                this.remoteFilePath = remoteFilePath;
                this.idx = idx;
                this.isDownload = isDownload;
            }

            @Override
            public void run() {
                try {
                    this.sendWaitCdl.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // waiting for calc job finish, then send redundant file block
                System.out.println("======== RS CALC CLIENT BEGIN SENDING REDUNDANT FILE BLOCK TO : " +
                        host + " ========");

                // ======== Test ========
                File sendFile = new File(sendFilePath + sendFileName);
                remoteSendStartPos = getRemoteSendStartPos(sendFilePath, sendFileName, idx, isDownload);

                SendPosition localSendPos = new SendPosition(0, sendFile.length());
                SendPosition remoteSendPos = new SendPosition(remoteSendStartPos, 0);
                String[] sendHost = new String[]{host};
                NioEventLoopGroup group = new NioEventLoopGroup(1);
                SendDataService sendDataService = new SendDataService(sendFile, localSendPos, remoteSendPos,
                        remoteFileName, remoteFilePath, sendHost, waitSendFinishCdl, group);
                sendDataService.call();

                // wait send redundant data
                try {
                    waitSendFinishCdl.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("======== SEND REDUNDANT DATA TO " + host + " FINISH ========");
                group.shutdownGracefully();

                // 删除RS计算临时文件
                if (!deleteFile(sendFilePath, sendFileName)){
                    System.err.println("======== DELETE RS CALC TEMP FILE FAILED ========");
                } else {
                    System.out.println("======== DELETE RS CALC TEMP FILE SUCCESSFULLY ========");
                }

                // 下载情况删除插值计算临时文件
                if (isDownload){
                   String lagCalcTempPath = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME).getValue("service.lag_decode_temp_path");
                   String lagCalcTempFileName = Util.geneTempName(sendFileName);
                   if (!deleteFile(lagCalcTempPath, lagCalcTempFileName)){
                       System.err.println("======== DELETE LAG CALC TEMP FILE FAILED ========");
                   } else {
                       System.out.println("======== DELETE LAG CALC TEMP FILE SUCCESSFULLY ========");
                   }
                }
            }

            private boolean deleteFile(String path, String fileName){
                File file = new File(path + fileName);
                if (file.exists()){
                    return file.delete();
                } else {
                    return false;
                }
            }

            private void startSendRedundantBlockClient(final String localFileName, final RedundancyBlockHeader header) throws Exception{
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
                                            .addLast(new SendRedundantBlockHandler(localFileName, header));
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
