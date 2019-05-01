package com.linghang.rpc.client;

import com.linghang.proto.Block;
import com.linghang.proto.BlockDetail;
import com.linghang.io.FileWriter;
import com.linghang.proto.RSCalcRequestHeader;
import com.linghang.proto.RedundancyBlockHeader;
import com.linghang.util.ConstantUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class ClientRSCalcHandler extends ChannelInboundHandlerAdapter {

    private long start;
    private RSCalcRequestHeader rsCalcRequestHeader;
    private FileWriter fileWriter;
    private CountDownLatch countDownLatch;
    private ChannelHandlerContext rpcContext;
    public static ConcurrentHashMap<String, Long> fileReadFlg = new ConcurrentHashMap<>();

    public ClientRSCalcHandler(RSCalcRequestHeader questHeader, CountDownLatch sendRedundantCdl, ChannelHandlerContext ctx) throws Exception{
        this.rsCalcRequestHeader = questHeader;
        this.start = questHeader.getStartPos();
        this.rpcContext = ctx;
        this.fileWriter = new FileWriter(questHeader.getFileName(), questHeader.getStartPos());
        this.countDownLatch = sendRedundantCdl;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        System.out.println("======== CONNECT TO RS CALC SERVER " +
                ctx.channel().remoteAddress().toString() + " ========");

        ctx.writeAndFlush(rsCalcRequestHeader);

        // 发送读取请求文件名
        System.out.println("======== RS CALC CLIENT SEND FILE BLOCK REQUEST FOR: " +
                rsCalcRequestHeader.getFileName() +
                "TO " + ctx.channel().remoteAddress().toString() + " ========");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof Block){
            Block fileBlock = (Block) msg;
            int readByte = fileBlock.getReadByte();
            byte[] buf = fileBlock.getBytes();
            System.out.println("======== CLIENT RECEIVE BYTE LENGTH : " + fileBlock.getReadByte() + " ========");

            // 将接收到的数据和本地数据进行异或相加
            fileWriter.write(start, buf, readByte);
            start = start + readByte;

            ctx.writeAndFlush(start);
        }

        if (msg instanceof Integer){
            // 将从文件传输服务器端传送的结果转发给rpc调用客户端，最后关闭文件传输连接
            Integer res = (Integer) msg;
            rpcContext.writeAndFlush(res);
            boolean closeSuccess = fileWriter.closeRF();
            if (!closeSuccess){
                System.out.println("======== " + ctx.channel().remoteAddress().toString() + " CLOSE RANDOM ACCESS FILE FAILED ========");
            }
            countDownLatch.countDown();
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

