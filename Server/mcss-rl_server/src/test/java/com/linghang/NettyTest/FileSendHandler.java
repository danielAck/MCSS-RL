package com.linghang.NettyTest;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

public class FileSendHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client channel active! ------- AT " + new Date());
        System.out.println("======= START SENDING FILE =======");
        File file = new File("F:\\WUST\\program\\dsz\\xx.txt");
        FileInputStream in = new FileInputStream(file);
        FileRegion fileRegion = new DefaultFileRegion(in.getChannel(), 0, file.length());
        ctx.write(fileRegion).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()){
                    System.out.println("======== FILE SENDING COMPLETE ========");
                } else {
                    System.out.println("======== FILE SENDING FAILED ========");
                }
            }
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf in) throws Exception {
        String response = in.toString(CharsetUtil.UTF_8);
        if (response.equals("OK")){
            System.out.println("======= SERVER RECEIVE SUCCESSFULLY =======");
        }
        else if (response.equals("ERROR")){
            System.out.println("======= ERROR EXIST IN SERVER =======");
        }
        else{
            System.out.println("======= SENDING ERROR =======");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
