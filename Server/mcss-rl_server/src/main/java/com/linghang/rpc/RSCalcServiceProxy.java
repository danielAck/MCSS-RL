package com.linghang.rpc;

import com.linghang.proto.RSCalcRequestHeader;
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
import java.util.concurrent.CountDownLatch;

public class RSCalcServiceProxy implements InvocationHandler{

    private String fileName;

    public RSCalcServiceProxy(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {

        // 开启 Demon线程 拉起3个service线程
//        Thread demonThread = new Thread(new RSCalcServiceDemon(fileName));
//        demonThread.setName(fileName + "-demon");
//        demonThread.start();

        Thread demonThread  = new Thread(new RSCalcServiceDemonTest(fileName));
        demonThread.setName(fileName + "-demon");
        demonThread.start();

        return new Object();
    }

    private static class RSCalcServiceDemon implements Runnable{

        CountDownLatch countDownLatch;
        String fileName;
        NioEventLoopGroup group;

        public RSCalcServiceDemon(String fileName) {
            this.fileName = fileName;
            this.countDownLatch = new CountDownLatch(3);
            this.group = new NioEventLoopGroup(1);
        }

        @Override
        public void run() {

            System.out.println("======== " + fileName + "-demon" + " THREAD BEGIN ========");
            PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);

            // 获取 Slave 结点IP
            String[] slaves = new String[3];
            slaves[0] = propertiesUtil.getValue("host.slave1");
            slaves[1] = propertiesUtil.getValue("host.slave2");
            slaves[2] = propertiesUtil.getValue("host.slave3");

            for (String host : slaves){

                // 从数据库中获取 host 对应的 id， 使用 id 进行计算 startPos
                long startPos = getCalcStartPos(host);

                Thread t = new Thread(new RSCalcServiceJob(fileName, host, startPos, group, countDownLatch));
                t.setName(fileName + "-thread");
                t.start();
            }

            try {
                countDownLatch.await();
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
        CountDownLatch countDownLatch;
        String fileName;
        NioEventLoopGroup group;

        public RSCalcServiceDemonTest(String fileName) {
            this.fileName = fileName;
            // demon 线程 countDownLatch
            this.countDownLatch = new CountDownLatch(1);
            this.group = new NioEventLoopGroup(1);
        }

        @Override
        public void run() {

            System.out.println("======== " + fileName + "-demon" + " THREAD BEGIN ========");
            PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);

            // 获取 RPC 结点IP, call RPC
            String[] slaves = new String[1];
            slaves[0] = "127.0.0.1";

            for (String host : slaves){

                long startPos = getCalcStartPos(host);

                Thread t = new Thread(new RSCalcServiceJob(fileName, host, startPos, group, countDownLatch));
                t.setName(fileName + "-thread");
                t.start();
            }

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("======== ALL JOB FINISHED ========");
            group.shutdownGracefully();

        }

        private long getCalcStartPos(String host){

            // TODO: 从数据库中获取host对应的id， 用 id 计算对应的 startPos

            return 0;
        }
    }

    private static class RSCalcServiceJob implements Runnable{

        CountDownLatch countDownLatch;
        CountDownLatch demon;
        NioEventLoopGroup group;
        String fileName;
        long startPos;
        String host;

        public RSCalcServiceJob(String fileName, String host, long startPos, NioEventLoopGroup group, CountDownLatch demon) {
            this.group = group;
            this.fileName = fileName;
            this.host = host;
            this.demon = demon;
            this.startPos = startPos;
            // 文件块请求 countDownLatch
            countDownLatch = new CountDownLatch(2);
        }

        @Override
        public void run() {

            System.out.println("======== " + fileName + "-" + host + "-job" + " JOB BEGIN ========");

            startClient(host, startPos, group);
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Job finished
            System.out.println("======== " + fileName + "-" + host + "-job" + " JOB FINISHED ========");
            demon.countDown();
        }

        // 启动RPC调用客户端
        public void startClient(String host, final long startPos, NioEventLoopGroup group){

            final RSCalcRequestHeader rsCalcRequestHeader = new RSCalcRequestHeader(fileName, startPos);

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
