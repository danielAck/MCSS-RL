package com.linghang.rpc.server;

import com.linghang.proto.Block;
import com.linghang.proto.BlockDetail;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.RandomAccessFile;

public class ServerReceiveFileBlockHandler extends ChannelInboundHandlerAdapter {

    private RandomAccessFile rf;
    private String fileName;
    private long start;
    private PropertiesUtil propertiesUtil;
    private String savePath;
    private boolean test;

    public ServerReceiveFileBlockHandler(boolean test) {
        this.propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
        this.test = test;
    }

    // 作为客户端，接收客户端上传的文件块
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        // receive send header
        if (msg instanceof BlockDetail){

            BlockDetail header = (BlockDetail) msg;

            // 接收新文件块，对相关数据进行初始化
            boolean initSuccess = init(header);
            if (!initSuccess){
                System.err.println("======== SERVER INIT RECEIVE JOB FAILED ========");
                ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
            }
        }

        // receive file block
        else if (msg instanceof Block){
            Block fileBlock = (Block) msg;
            try{
                rf.write(fileBlock.getBytes(), 0, fileBlock.getReadByte());
                System.out.println("======== SERVER RECEIVE " + fileBlock.getReadByte() + " BYTES FROM CLIENT =======");
            } catch (Exception e){
                handleError();
                ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
            }

            start = start + fileBlock.getReadByte();
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

    private Boolean init(BlockDetail header) throws Exception{
        fileName = header.getFileName();
        start = header.getStartPos();

        if (savePath == null){
            System.err.println("======== PLEASE SPECIFY PART SAVE PATH IN PROPERTY ========");
            return false;
        }

        // 判断接收的是否是计算生成的冗余块
        File file;
        if (header.isRedundant()){
            if (test){
                savePath = propertiesUtil.getValue("service.local_redundant_save_path");
            } else {
                savePath = propertiesUtil.getValue("service.redundant_save_path");
            }
            file = new File(savePath + Util.geneRedundancyName(fileName));
        } else {
            if (test){
                savePath = propertiesUtil.getValue("service.local_part_save_path");
            } else {
                savePath = propertiesUtil.getValue("service.part_save_path");
            }

            file = new File(savePath + Util.genePartName(fileName));
        }

        rf = new RandomAccessFile(file, "rw");
        rf.seek(start);

        System.out.println("======== SERVER BEGIN RECEIVE FILE : " + header.getFileName() + " ========");

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
        File file = new File(savePath + fileName);
        if (file.exists()){
            res = file.delete();
        }
        return res;
    }
}
