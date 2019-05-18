package com.linghang.rpc.server.handler;

import com.linghang.proto.Block;
import com.linghang.proto.BlockDetail;
import com.linghang.proto.RedundancyBlockHeader;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ServerReceiveBlockHandler extends ChannelInboundHandlerAdapter {

    private RandomAccessFile rf;
    private String fileName;
    private long start;
    private PropertiesUtil propertiesUtil;
    private String savePath;
    private boolean test;

    public ServerReceiveBlockHandler(boolean test) {
        this.propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
        this.test = test;
    }

    // 接收客户端上传的文件块，保存至本地
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        // receive normal file block save request
        if (msg instanceof BlockDetail){

            BlockDetail header = (BlockDetail) msg;
            this.fileName = header.getFileName();
            this.start = header.getStartPos();
            String savePath;

            // do init job
            if (test){
                savePath = propertiesUtil.getValue("service.local_part_save_path");
            } else {
                savePath = propertiesUtil.getValue("service.part_save_path");
            }
            if (savePath != null){

                init(Util.genePartName(fileName), start, savePath);
                write(header.getBytes(), header.getReadByte(), ctx);
                System.out.println("======== SERVER RECEIVE NORMAL FILE BLOCK SAVE REQUEST FOR : " + header.getFileName() + " ========");

                start = start + header.getReadByte();
                ctx.writeAndFlush(start);
            } else {
                System.err.println("======== SERVER INIT RECEIVE JOB FAILED ========");
                ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
            }
        }

        // receive redundant file block save request
        else if (msg instanceof RedundancyBlockHeader){
            RedundancyBlockHeader header = (RedundancyBlockHeader) msg;
            String savePath;
            fileName = header.getFileName();
            start = header.getStartPos();

            System.out.println("======== SERVER RECEIVE REDUNDANCY BLOCK HEADER FROM "
                    + ctx.channel().remoteAddress() + " FOR FILE :" + fileName + " ========");

            // do init job
            if (test){
                savePath = propertiesUtil.getValue("service.local_part_save_path");
            } else {
                savePath = propertiesUtil.getValue("service.part_save_path");
            }
            if (savePath != null){
                init(Util.genePartName(fileName), start, savePath);
                long start = 0;
                ctx.writeAndFlush(start);
            } else {
                System.err.println("======== YOU HAVE NOT SPECIFY THE SAVE PATH IN SERVER ========");
                ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
            }
        }

        // receive file block
        else if (msg instanceof Block){
            Block fileBlock = (Block) msg;
            write(fileBlock.getBytes(), fileBlock.getReadByte(), ctx);
            start = start + fileBlock.getReadByte();
            ctx.writeAndFlush(start);
        }

        // receive finish code
        else if (msg instanceof Integer){
            Integer res = (Integer) msg;

            if (res.equals(ConstantUtil.SEND_FINISH_CODE)){
                System.out.println("======== SERVER RECEIVE FILE BLOCK FINISH ========");
                rf.close();
            }

            else if (res.equals(ConstantUtil.SEND_ERROR_CODE)){
                System.err.println("======== ERROR OCCUR IN CLIENT ========");
                handleError();
            }
            ctx.close();
        }
        else {
            System.err.println("======== SERVER RECEIVE WRONG DATA TYPE ! ========");
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void write(byte[] bytes, int readByte, ChannelHandlerContext ctx){
        try{
            rf.write(bytes, 0, readByte);
            System.out.println("======== SERVER RECEIVE " + readByte + " BYTES FROM CLIENT =======");
        } catch (Exception e){
            handleError();
            ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
        }
    }

    private void init(String fileName, long start, String savePath) throws Exception{
        this.savePath = savePath;
        File file = new File(savePath + fileName);
        rf = new RandomAccessFile(file, "rw");
        rf.seek(start);
    }

    private void handleError() {
        if (rf != null){
            try {
                rf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        boolean deleteSuccess = deleteFile();
        if (deleteSuccess){
            System.out.println("======== SERVER DELETE FILE : " + fileName + " =========");
        } else {
            System.err.println("======== SERVER DELETE FILE : " + fileName + " FAILED =========");
        }
    }

    private boolean deleteFile(){
        boolean res = false;
        File file = new File(savePath + fileName);
        if (file.exists()){
            res = file.delete();
        }
        return res;
    }
}
