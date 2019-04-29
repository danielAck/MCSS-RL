package com.linghang.rpc.client;

import com.linghang.proto.BlockDetail;
import com.linghang.io.FileWriter;
import com.linghang.proto.RSCalcRequestHeader;
import com.linghang.util.ConstantUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.ConcurrentHashMap;

public class SendRedundancyHandler extends ChannelInboundHandlerAdapter {

    private ChannelHandlerContext rpcCtx;
    private String rsCalcHost;
    private RSCalcRequestHeader questHeader;

    public SendRedundancyHandler(RSCalcRequestHeader questHeader, String rsCalcHost, ChannelHandlerContext rpcCtx) {
        this.rpcCtx = rpcCtx;
        this.rsCalcHost = rsCalcHost;
        this.questHeader = questHeader;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        // 连接上接收端，启动计算请求客户端
        startRSCalcClient(ctx);

        System.out.println("======== START RS CALC CLIENT TO " +
                ctx.channel().remoteAddress().toString() + "========");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Integer){
            Integer res = (Integer) msg;

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void startRSCalcClient(final ChannelHandlerContext sendContext){
        Bootstrap b = new Bootstrap();
        b.group(rpcCtx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .remoteAddress(rsCalcHost, ConstantUtil.RS_CALC_SERVICE_PORT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new RSCalcClientHandler(questHeader, sendContext, rpcCtx));
                    }
                });
    }

    private static class RSCalcClientHandler extends ChannelInboundHandlerAdapter{

        private long start;
        private boolean isFirstReceiveData;
        private String questFileName;
        private ChannelHandlerContext sendRedundancyCtx;
        private ChannelHandlerContext rpcContext;
        private FileWriter fileWriter;
        private RSCalcRequestHeader questHeader;
        private BlockDetail redundantBlockDetail;
        public static ConcurrentHashMap<String, Long> fileReadFlg = new ConcurrentHashMap<>();

        public RSCalcClientHandler(RSCalcRequestHeader questHeader, ChannelHandlerContext sendRedundancyCtx, ChannelHandlerContext rpcContext) {
            this.questHeader = questHeader;
            this.sendRedundancyCtx = sendRedundancyCtx;
            this.rpcContext = rpcContext;
            this.questFileName = questHeader.getFileName();
            this.fileWriter = new FileWriter(questFileName);
            this.isFirstReceiveData = true;
            this.redundantBlockDetail = new BlockDetail(questFileName, true);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(questHeader);

            // 发送读取请求文件名
            System.out.println("======== RS CALC CLIENT SEND FILENAME " + "TO " +
                    ctx.channel().remoteAddress().toString() + " " + questFileName + " ========");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof BlockDetail){
                BlockDetail blockDetail = (BlockDetail) msg;
                int readByte = blockDetail.getReadByte();
                // 第一次接收数据
                if (isFirstReceiveData){
                    this.start = blockDetail.getStartPos();
                    isFirstReceiveData = false;
                }
                byte[] buf = blockDetail.getBytes();
                System.out.println("======== CLIENT RECEIVE BYTE LENGTH : " + blockDetail.getReadByte() + " ========");

                // 将接收到的数据和本地数据进行异或相加,并将计算结果发送到接收结点
                byte[] res = fileWriter.write(start, buf, readByte);
                redundantBlockDetail.setBytes(res);
                redundantBlockDetail.setReadByte(readByte);
                redundantBlockDetail.setStartPos(start);
                sendRedundancyCtx.writeAndFlush(redundantBlockDetail);

                start = start + readByte;
                ctx.writeAndFlush(start);
            }

            if (msg instanceof Integer){
                // 将从文件传输服务器端传送的结果转发给rpc调用客户端，最后关闭文件传输连接
                Integer res = (Integer) msg;
                rpcContext.writeAndFlush(res);
                boolean closeSuccess = fileWriter.closeRF();
                if (!closeSuccess){
                    System.out.println("======== " + ctx.channel().remoteAddress().toString() + " CLOSE RANDOM ACCESS FILE FAILED ========");
                }
                ctx.close();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
