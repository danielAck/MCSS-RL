package com.linghang.rpc;

import com.linghang.proto.LagCalcRequestHeader;
import com.linghang.proto.RSCalcRequestHeader;
import com.linghang.rpc.client.handler.LagCalcRPCHandler;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
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

    private String fileName;
    private String[] hosts;
    private String redundantBlockRecvHost;

    public RSCalcServiceProxy(String fileName, String[] hosts, String redundantBlockRecvHost) {
        this.fileName = fileName;
        this.hosts = hosts;
        this.redundantBlockRecvHost = redundantBlockRecvHost;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {

        // 开启 Demon线程 拉起3个service线程
//        Thread demonThread = new Thread(new RSCalcServiceDemon(fileName));
//        demonThread.setName(fileName + "-demon");
//        demonThread.start();

        Thread demonThread  = new Thread(new RSCalcServiceDemonTest(fileName, hosts, redundantBlockRecvHost));
        demonThread.setName(fileName + "-demon");
        demonThread.start();

        return new Object();
    }

    private static class RSCalcServiceDemon implements Runnable{

        private CountDownLatch demon;
        private CountDownLatch lagCountDownLatch;
        private String redundantBlockRecvHost;
        private NioEventLoopGroup group;
        private String[] slaves;
        String fileName;

        public RSCalcServiceDemon(String fileName, String[] slaves) {
            this.fileName = fileName;
            this.slaves = slaves;
            this.demon = new CountDownLatch(3);
            this.group = new NioEventLoopGroup(1);
        }

        @Override
        public void run() {

            System.out.println("======== " + fileName + "-demon" + " THREAD BEGIN ========");
            PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);

            for (String host : slaves){

                // TODO: 从数据库中获取 host 对应的 id， 使用 id 进行计算 startPos
                long startPos = getCalcStartPos(host);
                ArrayList<String> calcHosts = new ArrayList<>();
                for (String t : slaves){
                    if (!t.equals(host))
                        calcHosts.add(t);

                }

                RSCalcRequestHeader header = new RSCalcRequestHeader(fileName, calcHosts, redundantBlockRecvHost, startPos);
                Thread t = new Thread(new RSCalcServiceJob(host, header, group, demon));
                t.setName(fileName + "-thread");
                t.start();
            }

            try {
                demon.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("======== ALL JOB FINISHED ========");
            group.shutdownGracefully();

        }

        private long getCalcStartPos(String host){


            return 0;
        }
    }

    public static class RSCalcServiceDemonTest implements Runnable{
        CountDownLatch demon;

        String fileName;
        String redundantBlockRecvHost;
        String[] slaves;
        NioEventLoopGroup group;

        public RSCalcServiceDemonTest(String fileName, String[] slaves, String redundantBlockRecvHost) {
            this.fileName = fileName;
            this.slaves = slaves;
            this.redundantBlockRecvHost = redundantBlockRecvHost;
            // demon 线程 countDownLatch
            this.demon = new CountDownLatch(1);
            this.group = new NioEventLoopGroup(1);
        }

        @Override
        public void run() {

            System.out.println("======== " + fileName + "-demon" + " THREAD BEGIN ========");
            PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);

            // 获取 RPC 结点IP, call RPC
            for (String host : slaves){

                long startPos = getCalcStartPos(host);
                ArrayList<String> calcHosts = new ArrayList<>();
                for (String t : slaves){
                    if (!t.equals(host))
                        calcHosts.add(t);

                }
                RSCalcRequestHeader header = new RSCalcRequestHeader(fileName, calcHosts, redundantBlockRecvHost, startPos);

                Thread t = new Thread(new RSCalcServiceJob(host, header, group, demon));
                t.setName(fileName + "-thread");
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

//            // 冗余块计算完毕，开始依次调用远程Lag计算RPC接口, 使用同一个IO线程
//            // TODO: 可以选择在GUI中启动
//            NioEventLoopGroup lagGroup = new NioEventLoopGroup(1);
//            for (String host : slaves){
//                LagCalcRequestHeader header = new LagCalcRequestHeader(fileName, true);
//                callLagCalcRPC(lagGroup, host, header);
//            }
//
//            // 等待Lag计算完毕
//            try {
//                lagCountDownLatch.await();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("======== ALL LAG CALCULATION FINISH =========");

        }

        private long getCalcStartPos(String host){

            // TODO: 从数据库中获取host对应的id， 用 id 计算对应的 startPos

            return 0;
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
            // 文件块请求 countDownLatch
            countDownLatch = new CountDownLatch(1);
        }

        @Override
        public void run() {
            System.out.println("======== " + rsCalcRequestHeader.getFileName() + "-" + host + "-job" + " JOB BEGIN ========");

            startClient(group, rsCalcRequestHeader);
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Job finished
            System.out.println("======== " + rsCalcRequestHeader.getFileName() + "-" + host + "-job" + " JOB FINISHED ========");
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
                }
            }

            else{
                System.out.println("======== ERROR RESPONSE DATA TYPE ========");
            }

            countDownLatch.countDown();

            // 单个结点计算任务结束，关闭单个结点的rpc连接
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
