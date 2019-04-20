package com.linghang.rpc;

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
    private CountDownLatch countDownLatch;

    public RSCalcServiceProxy(String fileName) {
        this.fileName = fileName;
        this.countDownLatch = new CountDownLatch(3);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 开启 service 线程
        Thread serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                NioEventLoopGroup rsCalcGroup = new NioEventLoopGroup(1);
                PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);

                // 获取 Slave 结点
                String[] slaves = new String[3];
                slaves[0] = propertiesUtil.getValue("host.slave1");
                slaves[1] = propertiesUtil.getValue("host.slave2");
                slaves[2] = propertiesUtil.getValue("host.slave3");

                // 获取 service 对应端口
                int port = ConstantUtil.RS_CALC_RPC_PORT;

                // 对节点依次使用同一个 I/O 线程创建客户端连接
                for (String host : slaves){
                    startClient(host, port, rsCalcGroup);
                }

                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                rsCalcGroup.shutdownGracefully();
                System.out.println("Service 调用结束");
            }
        });
        serviceThread.start();

        return null;
    }

    public void startClient(String host, int port, NioEventLoopGroup group){
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(host, port)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new ObjectEncoder())
                                .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers
                                        .weakCachingConcurrentResolver(null)))
                                .addLast(new RSCalcClientHandler(countDownLatch, fileName));
                    }
                });
        b.connect();
    }

    private static class RSCalcClientHandler extends ChannelInboundHandlerAdapter {
        private CountDownLatch countDownLatch;
        private String fileName;

        public RSCalcClientHandler(CountDownLatch countDownLatch, String fileName) {
            this.countDownLatch = countDownLatch;
            this.fileName = fileName;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(fileName);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof Integer){
                Integer res = (Integer) msg;
                if (res.equals(ConstantUtil.RPC_JOB_FINISH_CODE)){
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
            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
