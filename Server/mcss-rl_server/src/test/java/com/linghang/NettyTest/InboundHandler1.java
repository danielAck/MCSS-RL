package com.linghang.NettyTest;

import com.linghang.io.BlockDetail;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Date;

public class InboundHandler1 extends ChannelInboundHandlerAdapter {

    private BlockDetail blockDetail;
    private int readByte;

    public InboundHandler1(BlockDetail blockDetail) {
        this.blockDetail = blockDetail;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("====== BEGINNING SENDING FILE ======" + new Date());

//        RandomAccessFile rf = new RandomAccessFile(blockDetail.getFile(), "r");
//        rf.seek(blockDetail.getStartPos());
//        int bufLength = 10*1024;
//        byte[] bytes = new byte[bufLength];
//
//        if((readByte = rf.read(bytes)) != -1){
//            blockDetail.setEndPos(readByte);
//            blockDetail.setBytes(bytes);
//            ctx.writeAndFlush(blockDetail);
//        }

        System.out.println("======= CHANNEL ACTIVE SEND " + readByte + " BYTES =======");
    }

    // Client
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Integer){
            Integer start = (Integer) msg;
            System.out.println("======= SERVER HAVE HANDLED " + start + " BYTES =======");

//            RandomAccessFile randomAccessFile = new RandomAccessFile(blockDetail.getFile(), "r");
//            randomAccessFile.seek(start);
//            byte[] bytes = new byte[10*1024];
//
//            if ((readByte = randomAccessFile.read(bytes)) != -1
//                    && (randomAccessFile.length() - start) > 0){
//
//                blockDetail.setBytes(bytes);
//                blockDetail.setEndPos(readByte);
//                ctx.writeAndFlush(blockDetail);
//            } else {
//                System.out.println("======= FILE SENDING FINISHED =======");
//                randomAccessFile.close();
//                ctx.close();
//            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("======= ERROR OCCUR IN CLIENT =======");
        cause.printStackTrace();
        ctx.close();             // 出现异常，关闭channel
    }
}
