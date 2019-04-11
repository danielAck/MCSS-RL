package com.linghang.SendFileTest;

import com.linghang.NettyTest.BlockDetail;
import com.linghang.pojo.JobDescription;
import com.linghang.pojo.SendFileJobDescription;
import com.linghang.util.ConstantUtil;
import com.linghang.util.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
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
        this.startPos = jobDescription.getStartPos();
        this.endPos = jobDescription.getEndPos();
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
        ctx.writeAndFlush(blockDetail);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Integer){
            int readByte;
            Integer serverReadCnt = (Integer) msg;

            // TODO: 多余的 0 byte 发送问题

            rf.seek(startPos + serverReadCnt);

            if ((readByte = rf.read(buf)) != -1
                    && (endPos - (startPos + serverReadCnt)) > 0){

                blockDetail.setBytes(buf);
                blockDetail.setReadByte(readByte);
                ctx.writeAndFlush(blockDetail);
            } else {
                rf.close();
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
