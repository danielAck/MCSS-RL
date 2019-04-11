package com.linghang.NettyTest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class InboundHandler3 extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        System.out.println("InboundHandler 3 : " + in.toString(CharsetUtil.UTF_8));
        ByteBuf writeData = Unpooled.copiedBuffer("Second response", CharsetUtil.UTF_8);
        ctx.write(writeData);
        ctx.fireChannelRead(msg);
    }
}
