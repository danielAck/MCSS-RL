package com.linghang.rpc;

import com.linghang.util.ConstantUtil;
import com.linghang.util.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.RandomAccessFile;

public class ServerReceiveHandler extends ChannelInboundHandlerAdapter {

    private BlockDetail blockDetail;
    private boolean isFirstReceive;
    private RandomAccessFile rf;
    private String fileName;
    private long start;

    public ServerReceiveHandler() {
        isFirstReceive = true;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // receive file block
        if (msg instanceof BlockDetail){

            blockDetail = (BlockDetail) msg;
            if (isFirstReceive){
                handleFirstReceive();
            }

            try{
                rf.write(blockDetail.getBytes(), 0, blockDetail.getReadByte());
                System.out.println("======== SERVER RECEIVE " + blockDetail.getReadByte() + " BYTES FROM CLIENT =======");
            } catch (Exception e){
                handleError();
                ctx.close();
                return;
            }

            start = start + blockDetail.getReadByte();
            ctx.writeAndFlush(start);
        }
        // receive finish code
        else if (msg instanceof Integer){
            Integer res = (Integer) msg;

            if (res.equals(ConstantUtil.SEND_FINISH_CODE)){
                System.out.println("======== SERVER SEND FINISH ========");
                rf.close();
            }

            else if (res.equals(ConstantUtil.SEND_ERROR_CODE)){
                handleError();
            }
            ctx.close();
        }
        else {
            System.err.println("======== SERVER RECEIVE ERROR TYPE OF DATA ! ========");
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private boolean deleteFile(){
        boolean res = false;
        File file = new File(ConstantUtil.CLIENT_PART_SAVE_PATH + fileName);
        if (file.exists()){
            res = file.delete();
        }
        return res;
    }

    private void handleFirstReceive() throws Exception{
        fileName = blockDetail.getFileName();
        start = 0;
        File file = new File(ConstantUtil.CLIENT_PART_SAVE_PATH + Util.genePartName(fileName));
        rf = new RandomAccessFile(file, "rw");
        rf.seek(0);
        System.out.println("======== SERVER BEGIN RECEIVE FILE : " + blockDetail.getFileName() + " ========");
        isFirstReceive = false;
    }

    private void handleError() throws Exception{
        System.out.println("======== SERVER SEND FAILED ========");
        rf.close();
        boolean deleteSuccess = deleteFile();
        if (deleteSuccess){
            System.out.println("======== SERVER DELETE FILE : " + fileName + " =========");
        } else {
            System.err.println("======== SERVER DELETE FILE : " + fileName + " FAILED =========");
        }
    }
}
