package com.linghang.rpc.server;

import com.linghang.io.BlockDetail;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
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
    private PropertiesUtil propertiesUtil;
    private String partSavePath;
    private boolean test;

    public ServerReceiveHandler(boolean test) {
        this.isFirstReceive = true;
        this.propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
        this.test = test;
    }

    // 作为客户端，接收客户端上传的文件块
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof BlockDetail){
            blockDetail = (BlockDetail) msg;

            // 接收新文件块，对相关数据进行初始化
            if (isFirstReceive){
                boolean initSuccess = init();
                if (!initSuccess){
                    System.err.println("======== SERVER INIT RECEIVE JOB FAILED ========");
                    ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
                }
            }

            try{
                rf.write(blockDetail.getBytes(), 0, blockDetail.getReadByte());
                System.out.println("======== SERVER RECEIVE " + blockDetail.getReadByte() + " BYTES FROM CLIENT =======");
            } catch (Exception e){
                handleError();
                ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
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
                System.err.println("======== ERROR OCCUR IN CLIENT ========");
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

    private Boolean init() throws Exception{
        fileName = blockDetail.getFileName();
        start = 0;

        if (test){
            partSavePath = propertiesUtil.getValue("service.local_part_save_path");
        } else {
            partSavePath = propertiesUtil.getValue("service.part_save_path");
        }

        if (partSavePath == null){
            System.err.println("======== PLEASE SPECIFY PART SAVE PATH IN PROPERTY ========");
            return false;
        }

        File file = new File(partSavePath + Util.genePartName(fileName));
        rf = new RandomAccessFile(file, "rw");
        rf.seek(0);

        System.out.println("======== SERVER BEGIN RECEIVE FILE : " + blockDetail.getFileName() + " ========");
        isFirstReceive = false;

        return true;
    }

    private void handleError() throws Exception{
        if (rf != null){
            rf.close();
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
        File file = new File(partSavePath + fileName);
        if (file.exists()){
            res = file.delete();
        }
        return res;
    }
}
