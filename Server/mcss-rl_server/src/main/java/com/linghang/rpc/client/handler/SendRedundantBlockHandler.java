package com.linghang.rpc.client.handler;

import com.linghang.proto.Block;
import com.linghang.proto.RedundancyBlockHeader;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SendRedundantBlockHandler extends ChannelInboundHandlerAdapter {

    private RedundancyBlockHeader header;
    private PropertiesUtil propertiesUtil;
    private Block block;
    private byte[] buf;
    private RandomAccessFile rf;

    public SendRedundantBlockHandler(RedundancyBlockHeader header) {
        this.header = header;
        this.block = new Block();
        propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
        this.buf = new byte[ConstantUtil.BUFLENGTH];
        init();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(header);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Long){
            Long data = (Long) msg;
            long readCnt = (data == 0 ? 0 : (data - header.getStartPos()));
            int readByte;
            rf.seek(readCnt);
            if ((readByte = rf.read(buf)) != -1){
                block.setBytes(buf);
                block.setReadByte(readByte);
                System.out.println("======= CLIENT SEND " + readByte + " BYTES FOR REDUNDANT BLOCK ========");
                ctx.writeAndFlush(block);
            } else {
                System.err.println("======== SERVER SEND REDUNDANT BLOCK FINISH ! =========");
                ctx.writeAndFlush(ConstantUtil.SEND_FINISH_CODE);
                closeRF();

                // delete calculate temp file
//                            boolean deleteSuccess = deleteCalcTempFile();
//                            if (!deleteSuccess){
//                                System.err.println("======== DELETE CALC TEMP FILE FAILED ========");
//                            }
            }
        }
        if (msg instanceof Integer){
            Integer res = (Integer) msg;
            if (res.equals(ConstantUtil.SEND_ERROR_CODE)){
                System.err.println("======== ERROR OCCUR IN RECEIVE SERVER ========");
                closeRF();
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void init(){
        String path = propertiesUtil.getValue("service.calc_temp_save_path");
        File file = new File(path + header.getRemoteFileName());
        try {
            rf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void closeRF(){
        try {
            rf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean deleteCalcTempFile(){
        String path = propertiesUtil.getValue("service.local_calc_temp_save_path");
        File file = new File(path + header.getRemoteFileName());
        if (file.exists()){
            return file.delete();
        }
        return false;
    }
}
