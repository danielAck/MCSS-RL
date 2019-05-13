package com.linghang.service;

import com.linghang.proto.LagCalcRequestHeader;
import com.linghang.rpc.client.handler.LagCalcRPCHandler;
import com.linghang.util.ConstantUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.concurrent.CountDownLatch;

public class LagCalcService implements Service{

    private String fileName;
    private CountDownLatch lagCountDownLatch;
    private String[] slaves;
    private boolean encode;

    public LagCalcService(String fileName, String[] slaves, boolean encode) {
        this.fileName = fileName;
        this.lagCountDownLatch = new CountDownLatch(slaves.length);
        this.slaves = slaves;
        this.encode = encode;
    }

    @Override
    public void call() {
        Thread t = new Thread(new LagCalcJob(encode));
        if (encode)
            t.setName(fileName + "-lagEncodeJob");
        else
            t.setName(fileName + "-lagDecodeJob");
        t.start();
    }

    private class LagCalcJob implements Runnable{

        private boolean encode;

        public LagCalcJob(boolean encode) {
            this.encode = encode;
        }

        @Override
        public void run() {

            if (encode)
                System.out.println("======== " + fileName + " LAGRANGE ENCODE CALCULATION JOB BEGIN ========");
            else
                System.out.println("======== " + fileName + " LAGRANGE DECODE CALCULATION JOB BEGIN ========");

            NioEventLoopGroup lagGroup = new NioEventLoopGroup(1);
            for (String host : slaves){
                LagCalcRequestHeader header = new LagCalcRequestHeader(fileName, encode);
                callLagCalcRPC(lagGroup, host, header);
            }

            // 等待Lag计算完毕
            try {
                lagCountDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("======== " + fileName + " LAG CALCULATION JOB FINISH =========");
            lagGroup.shutdownGracefully();
        }
    }

    private void callLagCalcRPC(NioEventLoopGroup group, String host, final LagCalcRequestHeader header){
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(host, ConstantUtil.LAG_CALC_RPC_PORT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new ObjectEncoder())
                                .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers
                                        .weakCachingConcurrentResolver(null)))
                                .addLast(new LagCalcRPCHandler(header, lagCountDownLatch));
                    }
                });
        b.connect();
    }
}
