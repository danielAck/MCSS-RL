package com.linghang.NettyTest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.FileRegion;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class FileReceiveHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Server channel active! ------ AT ------ " + new Date());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        File file = new File("");
        boolean createSuccess = file.createNewFile();
        if (createSuccess){
            FileOutputStream out = new FileOutputStream(file);
            FileRegion fileRegion = (FileRegion) msg;
            fileRegion.transferTo(out.getChannel(), fileRegion.count());

        } else {
            ByteBuf ERROR = Unpooled.copiedBuffer("ERROR", CharsetUtil.UTF_8);
            ctx.write(ERROR);
        }


    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
