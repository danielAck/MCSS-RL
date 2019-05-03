package com.linghang.rpc.server.handler;

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

        // receive calculation request
        if (msg instanceof RSCalcRequestHeader){

            RSCalcRequestHeader header = (RSCalcRequestHeader) msg;
            String fileName = header.getFileName();
            System.out.println("======= RS CALC SERVER CALC REQUEST FOR FILE: " + fileName + " =======");

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

        // receive client read process
        else if (msg instanceof Long){

            int readByte;
            long readCnt = (Long) msg;
            rf.seek(readCnt);
            long remainByte = length - readCnt;

            if ((readByte = rf.read(buf)) != -1
                    && remainByte > ConstantUtil.BUFLENGTH){

                fileBlock.setBytes(buf);
                fileBlock.setReadByte(readByte);
                ctx.writeAndFlush(fileBlock);
            }
            else if(remainByte > 0 && remainByte < ConstantUtil.BUFLENGTH) {

                fileBlock.setBytes(buf);
                fileBlock.setReadByte((int)remainByte);
                ctx.writeAndFlush(fileBlock);
            }
            else {
                rf.close();
                ctx.writeAndFlush(ConstantUtil.SEND_FINISH_CODE);
            }
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
