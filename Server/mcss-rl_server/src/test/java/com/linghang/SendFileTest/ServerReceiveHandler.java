package com.linghang.SendFileTest;


import com.linghang.NettyTest.BlockDetail;
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
                fileName = blockDetail.getFileName();
                start = 0;
                File file = new File(ConstantUtil.CLIENT_PART_SAVE_PATH + Util.genePartName(fileName));
                rf = new RandomAccessFile(file, "rw");
                rf.seek(0);
                isFirstReceive = false;
            }
            rf.write(blockDetail.getBytes(), 0, blockDetail.getReadByte());
            start = start + blockDetail.getReadByte();
            ctx.writeAndFlush(start);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
