package com.linghang.rpc;

import com.linghang.dao.UploadFileManageable;
import com.linghang.dao.impl.UploadFileManageImpl;
import com.linghang.proto.LagCalcRequestHeader;
import com.linghang.proto.RSCalcRequestHeader;
import com.linghang.rpc.client.handler.LagCalcRPCHandler;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class RSCalcServiceProxy implements InvocationHandler{

    private String calcFileName;
    private String calcFilePath;
    private String redundancySaveFileName;
    private String redundancySaveFilePath;
    private String[] hosts;
    private String redundantBlockRecvHost;
    private boolean isDownload;

    public RSCalcServiceProxy(String calcFileName, String calcFilePath, String redundancySaveFileName, String redundancySaveFilePath,
                              String[] hosts, String redundantBlockRecvHost, boolean isDownLoad) {
        this.calcFileName = calcFileName;
        this.calcFilePath = calcFilePath;
        this.redundancySaveFileName = redundancySaveFileName;
        this.redundancySaveFilePath = redundancySaveFilePath;
        this.hosts = hosts;
        this.redundantBlockRecvHost = redundantBlockRecvHost;
        this.isDownload = isDownLoad;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {

        // 开启 Demon线程 拉起3个service线程
//        Thread demonThread = new Thread(new RSCalcServiceDemon(fileName));
//        demonThread.setName(fileName + "-demon");
//        demonThread.start();

        Thread demonThread  = new Thread(new RSCalcServiceDemonTest(calcFileName, calcFilePath, redundancySaveFileName, redundancySaveFilePath,
                hosts, redundantBlockRecvHost, isDownload));
        demonThread.setName(calcFileName + "-demon");
        demonThread.start();

        return new Object();
    }

    public static class RSCalcServiceDemonTest implements Runnable{

        private String calcFileName;
        private String calcFilePath;
        private String redundancySaveFileName;
        private String redundancySaveFilePath;
        private CountDownLatch demon;
        private String redundantBlockRecvHost;
        private String[] slaves;
        private NioEventLoopGroup group;
        private boolean isDownload;

        public RSCalcServiceDemonTest(String calcFileName, String calcFilePath, String redundancySaveFileName, String redundancySaveFilePath,
                                      String[] slaves, String redundantBlockRecvHost, boolean isDownload) {

            this.calcFileName = calcFileName;
            this.calcFilePath = calcFilePath;
            this.redundancySaveFileName = redundancySaveFileName;
            this.redundancySaveFilePath = redundancySaveFilePath;
            this.slaves = slaves;
            this.redundantBlockRecvHost = redundantBlockRecvHost;
            // demon 线程 countDownLatch
            this.demon = new CountDownLatch(slaves.length);
            this.group = new NioEventLoopGroup(1);
            this.isDownload = isDownload;
        }

        @Override
        public void run() {

            System.out.println("======== " + calcFileName + "-demon" + " THREAD BEGIN ========");
            PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);

            // 获取 RPC 结点IP, 拉起 3 个 RPC 连接
            int[] blockIds = getCalcBlockIdx(slaves, isDownload);
            for (int i = 0; i < slaves.length; i++){
                int blockIdx = blockIds[i];
                if (blockIdx < 0){
                    System.err.println("========= ERROR OCCUR IN DB CONNECTION =========");
                    return;
                }
                ArrayList<String> calcHosts = new ArrayList<>();
                for (String t : slaves){
                    if (!t.equals(slaves[i]))
                        calcHosts.add(t);
                }

                RSCalcRequestHeader header = new RSCalcRequestHeader(calcFileName, calcFilePath, redundancySaveFileName,
                        redundancySaveFilePath, calcHosts, redundantBlockRecvHost, blockIdx, isDownload);
                Thread t = new Thread(new RSCalcServiceJob(slaves[i], header, group, demon));
                t.setName(calcFileName + "-thread");
                t.start();
            }

            // 等待冗余块计算完毕
            try {
                demon.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("======== ALL RS CALC JOB FINISHED ========");
            group.shutdownGracefully();

        }

        // TODO: 根据数据库 cloudId 确定计算任务
        private int[] getCalcBlockIdx(String[] hosts, boolean isDownload){
            UploadFileManageable uploadFileService = new UploadFileManageImpl();
            String uploadFileName = Util.getFileUploadName(calcFileName);
            int[] res = new int[hosts.length];

            if (!isDownload){
                for (int i = 0; i < hosts.length; i++){
                    res[i] = uploadFileService.getCloudIdByFileNameAndHost(uploadFileName, hosts[i]);
                }
                return res;
            } else {
                return new int[]{0, 1, 2};
            }
        }

    }

    private static class RSCalcServiceJob implements Runnable{

        private CountDownLatch countDownLatch;
        private CountDownLatch demon;
        private NioEventLoopGroup group;
        private String host;
        private RSCalcRequestHeader rsCalcRequestHeader;

        public RSCalcServiceJob(String host, RSCalcRequestHeader rsCalcRequestHeader, NioEventLoopGroup group, CountDownLatch demon) {
            this.group = group;
            this.demon = demon;
            this.host = host;
            this.rsCalcRequestHeader = rsCalcRequestHeader;
            // host RS calc countDownLatch
            countDownLatch = new CountDownLatch(1);
        }

        @Override
        public void run() {
            System.out.println("======== " + rsCalcRequestHeader.getCalcFileName() + "-" + host + "-job" + " JOB BEGIN ========");

            startClient(group, rsCalcRequestHeader);
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Job finished
            System.out.println("======== " + rsCalcRequestHeader.getCalcFileName() + "-" + host + "-job" + " JOB FINISHED ========");
            demon.countDown();
        }

        // 启动RPC调用客户端
        public void startClient(NioEventLoopGroup group, final RSCalcRequestHeader rsCalcRequestHeader){

            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(host, ConstantUtil.RS_CALC_RPC_PORT)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new ObjectEncoder())
                                    .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers
                                            .weakCachingConcurrentResolver(null)))
                                    .addLast(new RSCalcRPCClientHandler(countDownLatch, rsCalcRequestHeader));
                        }
                    });
            b.connect();
        }
    }

    private static class RSCalcRPCClientHandler extends ChannelInboundHandlerAdapter {
        private CountDownLatch countDownLatch;
        private RSCalcRequestHeader questHeader;

        public RSCalcRPCClientHandler(CountDownLatch countDownLatch, RSCalcRequestHeader questHeader) {
            this.countDownLatch = countDownLatch;
            this.questHeader = questHeader;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {

            // 发送计算请求头
            ctx.writeAndFlush(questHeader);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof Integer){
                Integer res = (Integer) msg;
                if (res.equals(ConstantUtil.SEND_FINISH_CODE)){
                    System.out.println("======== " + ctx.channel().remoteAddress().toString() + " FINISH RPC CALL ========");
                }
                else{
                    System.out.println("======== ERROR RESPONSE CODE ========");
                    ctx.close();
                }
            }

            else{
                System.out.println("======== ERROR RESPONSE DATA TYPE ========");
            }

            countDownLatch.countDown();

            // 结点计算任务结束，关闭rpc连接
            if (countDownLatch.getCount() == 0){
                ctx.close();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
