package com.linghang.rpc.client.handler;

import com.linghang.proto.Block;
import com.linghang.proto.BlockDetail;
import com.linghang.io.FileWriter;
import com.linghang.proto.RSCalcRequestHeader;
import com.linghang.proto.RedundancyBlockHeader;
import com.linghang.util.ConstantUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

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

        System.out.println("======== CONNECT TO REDUNDANCY RECEIVE SERVER " +
                ctx.channel().remoteAddress().toString() + " ========");

        RedundancyBlockHeader redundancyBlockHeader = new RedundancyBlockHeader(questHeader.getFileName(), questHeader.getStartPos());

        // 启动计算请求客户端
        startRSCalcClient(ctx);

        System.out.println("======== START RS CALC CLIENT TO " +
                ctx.channel().remoteAddress().toString() + "========");

        ctx.writeAndFlush(redundancyBlockHeader);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        // TODO：考虑怎么确保冗余块接收端正常消费完数据

        if (msg instanceof Integer){
            Integer res = (Integer) msg;
            if (res.equals(ConstantUtil.SEND_ERROR_CODE)){
                System.err.println("======== ERROR OCCUR IN REDUNDANT FILE BLOCK RECEIVE SERVER ========");
                rpcCtx.writeAndFlush(res);
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void startRSCalcClient(final ChannelHandlerContext sendRedundancyCtx){
        Bootstrap b = new Bootstrap();
        b.group(rpcCtx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .remoteAddress(rsCalcHost, ConstantUtil.RS_CALC_SERVICE_PORT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers
                                        .weakCachingConcurrentResolver(null)))
                                .addLast(new ObjectEncoder())
                                .addLast(new RSCalcClientHandler(questHeader, sendRedundancyCtx, rpcCtx));
                    }
                });
    }

    private static class RSCalcClientHandler extends ChannelInboundHandlerAdapter{

        private long start;
        private FileWriter fileWriter;
        private String questFileName;
        private Block redundantBlock;
        private ChannelHandlerContext sendRedundancyCtx;
        private ChannelHandlerContext rpcContext;
        private RSCalcRequestHeader rsCalcRequestHeader;

        public static ConcurrentHashMap<String, Long> fileReadFlg = new ConcurrentHashMap<>();

        public RSCalcClientHandler(RSCalcRequestHeader questHeader, ChannelHandlerContext sendRedundancyCtx, ChannelHandlerContext rpcContext) {
            this.rsCalcRequestHeader = questHeader;
            this.start = questHeader.getStartPos();
            this.sendRedundancyCtx = sendRedundancyCtx;
            this.rpcContext = rpcContext;
            this.questFileName = questHeader.getFileName();
//            this.fileWriter = new FileWriter(questFileName);
            this.redundantBlock = new Block();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {

            System.out.println("======== CONNECT TO RS CALC SERVER " +
                    ctx.channel().remoteAddress().toString() + " ========");

            ctx.writeAndFlush(rsCalcRequestHeader);

            // 发送读取请求文件名
            System.out.println("======== RS CALC CLIENT SEND FILENAME " + "TO " +
                    ctx.channel().remoteAddress().toString() + " " + questFileName + " ========");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof Block){
                Block fileBlock = (Block) msg;
                int readByte = fileBlock.getReadByte();
                byte[] buf = fileBlock.getBytes();
                System.out.println("======== CLIENT RECEIVE BYTE LENGTH : " + fileBlock.getReadByte() + " ========");

                // 将接收到的数据和本地数据进行异或相加,并将计算结果发送到接收结点
                fileWriter.write(start, buf, readByte);
//                redundantBlock.setBytes(calcResult);
//                redundantBlock.setReadByte(readByte);

                start = start + readByte;

                // 发送计算结果至冗余块接收端
                sendRedundancyCtx.writeAndFlush(redundantBlock);

                // 发送接收进度至文件块发送端
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
