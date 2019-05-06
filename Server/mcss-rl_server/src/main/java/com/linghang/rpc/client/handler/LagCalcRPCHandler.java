package com.linghang.rpc.client.handler;

import com.linghang.proto.LagCalcRequestHeader;
import com.linghang.util.ConstantUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.CountDownLatch;

public class LagCalcRPCHandler extends ChannelInboundHandlerAdapter {

    private CountDownLatch countDownLatch;
    private String fileName;

    public LagCalcRPCHandler(String fileName, CountDownLatch countDownLatch) {
        this.fileName = fileName;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LagCalcRequestHeader header = new LagCalcRequestHeader(fileName, true);
        ctx.writeAndFlush(header);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Integer){
            Integer res = (Integer) msg;
            if (res.equals(ConstantUtil.CALC_FINISH_CODE)){
                System.out.println("======== " + ctx.channel().remoteAddress().toString() + " CALC JOB FINISHING ========");
                countDownLatch.countDown();
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
