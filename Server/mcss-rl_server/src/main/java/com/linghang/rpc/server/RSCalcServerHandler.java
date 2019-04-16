package com.linghang.rpc.server;

import com.linghang.proto.RSCalcRequestHeader;
import com.linghang.pojo.CalcExecutor;
import com.linghang.util.ConstantUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class RSCalcServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof RSCalcRequestHeader){

            CalcExecutor executor = new CalcExecutor();
            boolean res = executor.execute();
            ctx.writeAndFlush(res);
        }

        else{
            System.err.println("======== SERVER RECEIVE WORN DATA TYPE ========");
            ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
