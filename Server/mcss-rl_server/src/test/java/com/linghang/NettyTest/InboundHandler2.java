package com.linghang.NettyTest;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.RandomAccessFile;


public class InboundHandler2 extends ChannelInboundHandlerAdapter {

    private static int cnt = 0;
    private final String uploadPath = "F:\\WUST\\program\\dsz\\download\\";
    private int start = 0;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("====== SERVER CHANNEL ACTIVE =======");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    // Server
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof BlockDetail){
            BlockDetail blockDetail = (BlockDetail) msg;
            int readByte = blockDetail.getEndPos();

            if (readByte > 0){
                System.out.println("======= " + (cnt++) + " SERVER RECEIVE " + readByte + " BYTES =======");
                String fileName = blockDetail.getFileName();
                File file = new File(uploadPath + fileName);
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.seek(start);
                if (readByte < 1024*10){
                    randomAccessFile.write(blockDetail.getBytes(), 0, readByte);
                } else {
                    randomAccessFile.write(blockDetail.getBytes());
                }

                start = start + readByte;
                ctx.writeAndFlush(start);
                randomAccessFile.close();
            } else {
                System.out.println("======= SERVER RECEIVE -1, CLOSE CONNECTION =======");
                ctx.close();
            }
        }
    }
}
