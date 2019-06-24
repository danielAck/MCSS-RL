package com.linghang.service;

import com.linghang.pojo.SendPosition;
import com.linghang.proto.Block;
import com.linghang.proto.BlockHeader;
import com.linghang.util.ConstantUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;

public class SendDataService implements Service {

    private File file;
    private SendPosition localSendPos;
    private SendPosition remoteSendPos;
    private String remoteFileName;
    private String remoteFilePath;
    private String[] hosts;
    private CountDownLatch sendCdl;
    private NioEventLoopGroup group;

    public SendDataService(File sendFile, SendPosition localSendPos, SendPosition remoteSendPos,
                           String remoteFileName, String remoteFilePath, String[] hosts, CountDownLatch sendCdl, NioEventLoopGroup group) {
        this.localSendPos = localSendPos;
        this.remoteSendPos = remoteSendPos;
        this.file = sendFile;
        this.remoteFileName = remoteFileName;
        this.remoteFilePath = remoteFilePath;
        this.hosts = hosts;
        this.sendCdl = sendCdl;
        this.group = group;
    }

    @Override
    public void call() {
        for (String host : hosts){
            createSendDataClient(host);
        }
    }

    private void createSendDataClient(String host){
        Bootstrap b = new Bootstrap();
        b.group(group)
                .remoteAddress(host, ConstantUtil.SEND_FILE_SERVICE_PORT)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers
                                        .weakCachingConcurrentResolver(null)))
                                .addLast(new ObjectEncoder())
                                .addLast(new SendDataClientHandler());
                    }
                });
        b.connect();
    }

    private class SendDataClientHandler extends ChannelInboundHandlerAdapter {

        byte[] buf;
        RandomAccessFile rf;
        Block block = new Block();

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("========= CONNECT TO SEND REMOTE HOST : " + ctx.channel().remoteAddress() + " =========");
            boolean initSuccess = init();
            if (!initSuccess) {
                System.out.println("======== INIT SEND DATA SERVICE FAILED ========");
                ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
            }

            BlockHeader header = new BlockHeader(remoteFileName, remoteFilePath, remoteSendPos);
            ctx.writeAndFlush(header);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof Integer) {
                Integer res = (Integer) msg;
                if (res.equals(ConstantUtil.START_SEND_CODE)) {
                    read(ctx, localSendPos.getStartPos());
                } else {
                    System.err.println("========= RECEIVE ERROR DATA TYPE ========");
                    ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
                    rf.close();
                    sendCdl.countDown();
                }
            } else if (msg instanceof Long) {
                long res = localSendPos.getStartPos() + ((Long) msg - remoteSendPos.getStartPos());
                rf.seek(res);
                read(ctx, res);
            } else {
                System.out.println("======== GET WRONG DATA TYPE =========");
                ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
                rf.close();
                sendCdl.countDown();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

        private boolean init() {

            // init buf
            buf = new byte[ConstantUtil.BUFLENGTH];

            // init random access file
            try {
                rf = new RandomAccessFile(file, "r");
                rf.seek(localSendPos.getStartPos());
            } catch (FileNotFoundException e) {
                System.err.println("========= SENDING FILE DO NOT EXIST ! ========");
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                System.err.println("======== SEEKING FILE FAILED ========");
                e.printStackTrace();
                return false;
            }

            return true;
        }

        /**
         * 从本地文件中读取数据并发送到目的主机
         *
         * @param ctx 网络连接
         */
        private void read(ChannelHandlerContext ctx, long res) throws Exception {

            int readByte;
            long remainByteCnt = localSendPos.getEndPos() - res;

            if ((readByte = rf.read(buf)) != -1
                    && remainByteCnt > 0) {

                block.setBytes(buf);
                if (readByte > remainByteCnt) {
                    block.setReadByte((int) remainByteCnt);
                } else {
                    block.setReadByte(readByte);
                }

                System.out.println("======= CLIENT SEND " + readByte + " BYTES ========");
                ctx.writeAndFlush(block);
            } else {
                if (remainByteCnt > 0) {
                    if (remainByteCnt < Integer.MAX_VALUE) {
                        byte[] redundantBytes = new byte[(int) remainByteCnt];
                        block.setBytes(redundantBytes);
                        block.setReadByte((int) remainByteCnt);
                        System.out.println("======= CLIENT SEND " + remainByteCnt + " BYTES ========");
                        ctx.writeAndFlush(block);
                    } else {
                        System.err.println("======== SERVER SEND WORN READ BYTE COUNT ! =========");
                        ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
                        rf.close();
                        sendCdl.countDown();
                    }
                } else {
                    rf.close();
                    ctx.writeAndFlush(ConstantUtil.SEND_FINISH_CODE);
                    sendCdl.countDown();
                }
            }
        }
    }
}
