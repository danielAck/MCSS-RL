package com.linghang.NettyTest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.util.Date;

public class ClientInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static int cnt = 0;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client Active！ " + new Date());
        ctx.writeAndFlush(
                Unpooled.copiedBuffer("Client quest!", CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        System.out.println("Client received msg: " + byteBuf.toString(CharsetUtil.UTF_8));
        if (cnt == 1)
            channelHandlerContext.close();
        else
            cnt++;
    }
}
