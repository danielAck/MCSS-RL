package com.linghang.rpc.client;

import com.linghang.pojo.SendFileJobDescription;
import com.linghang.proto.BlockDetail;
import com.linghang.util.ConstantUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.RandomAccessFile;

public class ClientSendHandler extends ChannelInboundHandlerAdapter {

    private SendFileJobDescription jobDescription;
    private long startPos;
    private long endPos;
    private BlockDetail blockDetail;
    private RandomAccessFile rf;
    private byte[] buf;

    public ClientSendHandler(SendFileJobDescription jobDescription) {
        this.jobDescription = jobDescription;
        this.startPos = jobDescription.getSendPosition().getStartPos();
        this.endPos = jobDescription.getSendPosition().getEndPos();
        initBlockDetail();
    }

    private void initBuf(){
        this.buf = new byte[ConstantUtil.BUFLENGTH];
    }

    private void initBlockDetail(){
        blockDetail = new BlockDetail();
        blockDetail.setFileName(jobDescription.getFileName());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        initBuf();

        // read file
        String file = jobDescription.getFilePath()+jobDescription.getFileName();
        rf = new RandomAccessFile(file, "r");

        rf.seek(startPos);
        int readByte = rf.read(buf);

        blockDetail.setReadByte(readByte);
        blockDetail.setBytes(buf);

        System.out.println("======= CLIENT BEGIN SEND FILE : " + jobDescription.getFileName() + " ========");
        System.out.println("======= CLIENT SEND " + readByte + " BYTES ========");
        ctx.writeAndFlush(blockDetail);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Long){
            int readByte;
            Long serverReadCnt = (Long) msg;

            rf.seek(startPos + serverReadCnt);
            long remainByteCnt = endPos - (startPos + serverReadCnt);

            if ((readByte = rf.read(buf)) != -1
                    && remainByteCnt > 0){

                blockDetail.setBytes(buf);
                if (readByte > remainByteCnt){
                    blockDetail.setReadByte((int)remainByteCnt);
                } else {
                    blockDetail.setReadByte(readByte);
                }

                System.out.println("======= CLIENT SEND " + readByte + " BYTES ========");
                ctx.writeAndFlush(blockDetail);
            } else {
                if (remainByteCnt > 0){
                    if (remainByteCnt < Integer.MAX_VALUE){
                        byte[] redundantBytes = new byte[(int)remainByteCnt];
                        blockDetail.setBytes(redundantBytes);
                        blockDetail.setReadByte((int)remainByteCnt);
                        ctx.writeAndFlush(blockDetail);
                    } else {
                        System.err.println("======== SERVER SEND WORN READ BYTE COUNT ! =========");
                        ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
                    }
                } else {
                    rf.close();
                    ctx.writeAndFlush(ConstantUtil.SEND_FINISH_CODE);
                }
            }
        }

        // handle error
        else if (msg instanceof Integer){
            Integer res = (Integer) msg;
            if (res.equals(ConstantUtil.SEND_ERROR_CODE)){
                System.err.println("======== ERROR OCCUR IN SERVER ========");
                ctx.close();
            }
        }

        else {
            System.err.println("======== SERVER RECEIVE ERROR TYPE OF DATA ! ========");
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
