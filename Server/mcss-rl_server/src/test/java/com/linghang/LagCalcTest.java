package com.linghang;

import com.linghang.proto.LagCalcRequestHeader;
import com.linghang.rpc.client.handler.LagCalcRPCHandler;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;

public class LagCalcTest {

    private CountDownLatch lagCountDownLatch = new CountDownLatch(1);

    public void doCalc(String fileName, boolean encode){

        NioEventLoopGroup lagGroup = new NioEventLoopGroup(1);
        String[] slaves = {"127.0.0.1"};
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
        System.out.println("======== ALL LAG CALCULATION FINISH =========");
        lagGroup.shutdownGracefully();
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

    public static void main(String[] args) {
        LagCalcTest lagCalcTest = new LagCalcTest();
        String fileName = "1M.pdf";
        lagCalcTest.doCalc(fileName, false);
    }


}
