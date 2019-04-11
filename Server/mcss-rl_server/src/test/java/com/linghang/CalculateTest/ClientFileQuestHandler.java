package com.linghang.CalculateTest;

import com.linghang.NettyTest.BlockDetail;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientFileQuestHandler extends ChannelInboundHandlerAdapter {

    private static final int bufLength = 10240;
    private boolean isFirstReceiveData;
    private String questFileName;
    private byte[] buf;
    private long start;
    private FileWriter fileWriter;

    public ClientFileQuestHandler(String questFileName) throws Exception{
        this.questFileName = questFileName;
        this.start = 0;
        this.isFirstReceiveData = true;
        fileWriter = new FileWriter(questFileName);
    }

    public void setFileName(String fileName) {
        this.questFileName = fileName;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 发送读取请求文件名
        ctx.writeAndFlush(questFileName);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof BlockDetail){
            BlockDetail blockDetail = (BlockDetail) msg;
            int readByte = blockDetail.getReadByte();
            // 第一次接收数据
            if (isFirstReceiveData){
                this.start = blockDetail.getStartPos();
                isFirstReceiveData = false;
            }
            buf = blockDetail.getBytes();
            System.out.println("======== CLIENT RECEIVE BYTE LENGTH : " + blockDetail.getReadByte() + " ========");

            fileWriter.write(start, buf, readByte);
            start = start + readByte;

            ctx.writeAndFlush(start);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
