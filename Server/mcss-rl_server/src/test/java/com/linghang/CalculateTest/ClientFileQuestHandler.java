package com.linghang.CalculateTest;

import NettyTest.BlockDetail;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientFileQuestHandler extends ChannelInboundHandlerAdapter {

    private static final int bufLength = 10240;
    private boolean receivedData;
    private String questFileName;
    private byte[] buf;
    private int start;
    private FileWriter fileWriter;

    public ClientFileQuestHandler(String questFileName) throws Exception{
        this.questFileName = questFileName;
        this.start = 0;
        this.receivedData = false;
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
            int readByte = blockDetail.getEndPos();
            // 第一次接收数据
            if (!receivedData){
                this.start = blockDetail.getStartPos();
                receivedData = true;
            }
            buf = blockDetail.getBytes();
            System.out.println("======== CLIENT RECEIVE BYTE LENGTH : " + blockDetail.getEndPos() + " ========");

            // TODO: 将 byte[] 写入磁盘
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
