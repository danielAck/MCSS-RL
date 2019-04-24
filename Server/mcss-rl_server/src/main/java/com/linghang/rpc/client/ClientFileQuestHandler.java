package com.linghang.rpc.client;

import com.linghang.io.BlockDetail;
import com.linghang.io.FileWriter;
import com.linghang.util.ConstantUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ConcurrentHashMap;

public class ClientFileQuestHandler extends ChannelInboundHandlerAdapter {

    private boolean isFirstReceiveData;
    private String questFileName;
    private long start;
    private FileWriter fileWriter;
    private ChannelHandlerContext rpcContext;
    public static ConcurrentHashMap<String, Long> fileReadFlg = new ConcurrentHashMap<>();

    public ClientFileQuestHandler(String questFileName, ChannelHandlerContext ctx) throws Exception{

        this.rpcContext = ctx;
        this.questFileName = questFileName;
        this.start = 0;
        this.isFirstReceiveData = true;
        this.fileWriter = new FileWriter(questFileName);
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
            byte[] buf = blockDetail.getBytes();
            System.out.println("======== CLIENT RECEIVE BYTE LENGTH : " + blockDetail.getReadByte() + " ========");

            // 将接收到的数据和本地数据进行异或相加
            fileWriter.write(start, buf, readByte);
            start = start + readByte;

            ctx.writeAndFlush(start);
        }

        if (msg instanceof Integer){
            // 将从文件传输服务器端传送的结果转发给rpc调用客户端，最后关闭文件传输连接
            Integer res = (Integer) msg;
            rpcContext.writeAndFlush(res);

            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
