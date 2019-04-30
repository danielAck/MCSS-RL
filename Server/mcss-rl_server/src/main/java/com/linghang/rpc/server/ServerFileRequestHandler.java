package com.linghang.rpc.server;

import com.linghang.proto.Block;
import com.linghang.proto.BlockDetail;
import com.linghang.proto.RSCalcRequestHeader;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.RandomAccessFile;

public class ServerFileRequestHandler extends ChannelInboundHandlerAdapter {

    private static final int bufLength = ConstantUtil.BUFLENGTH;
    private Block fileBlock;
    private RandomAccessFile rf;
    private PropertiesUtil propertiesUtil;
    private long length;
    private byte[] buf;

    public ServerFileRequestHandler() {
        fileBlock = null;
        rf = null;
        propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
        buf = new byte[bufLength];
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        // 获取到获取文件请求
        if (msg instanceof RSCalcRequestHeader){

            RSCalcRequestHeader header = (RSCalcRequestHeader) msg;
            String fileName = header.getFileName();
            System.out.println("======= RS CALC RECEIVE FILE NAME: " + fileName + " =======");

            // 将文件分块发送到客户端
            fileBlock = new Block();

            String saveFileName = Util.genePartName(fileName);
            String path = propertiesUtil.getValue("service.local_part_save_path");
            File file = new File(path + saveFileName);
            if (!file.exists()){
                System.out.println("======= ERROR : QUEST FILE DOESN'T EXIST IN SERVER ========");
                ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
            }

            rf = new RandomAccessFile(file, "r");
            rf.seek(header.getStartPos());

            // TODO: 看怎么处理这个 length 好一些
            this.length = rf.length() / 3;

            int readByte = rf.read(buf);
            fileBlock.setBytes(buf);
            fileBlock.setReadByte(readByte);

            ctx.writeAndFlush(fileBlock);
        }

        // 收到客户端的读取情况
        else if (msg instanceof Long){

            int readByte;
            long start = (Long) msg;
            rf.seek(start);

            if ((readByte = rf.read(buf)) != -1
                    && (length - start) > 0){

                fileBlock.setBytes(buf);
                fileBlock.setReadByte(readByte);
                ctx.writeAndFlush(fileBlock);
            } else {
                rf.close();
                ctx.writeAndFlush(ConstantUtil.SEND_FINISH_CODE);
            }
        }

        else if (msg instanceof Integer){
            System.out.println("======== RS CALC SERVER RECEIVE WRONG DATA TYPE : Integer ========");
            ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
        }

        else{
            System.out.println("======== RS CALC SERVER RECEIVE WRONG DATA TYPE ========");
            ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        rf.close();
        ctx.close();
    }
}
