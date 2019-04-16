package com.linghang.rpc.server;

import com.linghang.io.BlockDetail;
import com.linghang.util.ConstantUtil;
import com.linghang.util.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.RandomAccessFile;

public class ServerFileRequestHandler extends ChannelInboundHandlerAdapter {

    private static final int bufLength = ConstantUtil.BUFLENGTH;
    private BlockDetail blockDetail;
    private RandomAccessFile randomAccessFile;
    private byte[] buf;

    public ServerFileRequestHandler() {
        blockDetail = null;
        randomAccessFile = null;
        buf = new byte[bufLength];
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 获取到获取文件请求
        if (msg instanceof String){

            String fileName = (String) msg;
            System.out.println("======= SERVER RECEIVE FILE NAME: " + fileName + " =======");

            // 将文件分块发送到客户端
            blockDetail = new BlockDetail();
            blockDetail.setStartPos(0);
            String saveFileName = Util.genePartName(fileName);
            File file = new File(ConstantUtil.CLIENT_PART_SAVE_PATH + saveFileName);
            if (!file.exists()){
                System.out.println("======= ERROR : QUEST FILE DOESN'T EXIST IN SERVER ========");
                ctx.close();
            }
            randomAccessFile = new RandomAccessFile(file, "r");

            int readByte = randomAccessFile.read(buf);
            blockDetail.setBytes(buf);
            blockDetail.setReadByte(readByte);

            ctx.writeAndFlush(blockDetail);

        }
        // 收到客户端的读取情况
        if (msg instanceof Integer){
            int readByte;
            Integer start = (Integer) msg;
            randomAccessFile.seek(start);

            if ((readByte = randomAccessFile.read(buf)) != -1
                    && (randomAccessFile.length() - start) > 0){

                blockDetail.setBytes(buf);
                blockDetail.setReadByte(readByte);
                ctx.writeAndFlush(blockDetail);
            } else {
                randomAccessFile.close();
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        randomAccessFile.close();
        ctx.close();
    }
}
