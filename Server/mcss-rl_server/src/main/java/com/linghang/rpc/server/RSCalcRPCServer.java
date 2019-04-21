package com.linghang.rpc.server;

import com.linghang.rpc.client.ClientFileQuestHandler;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class RSCalcRPCServer {


    public RSCalcRPCServer() {
    }

    public void start() throws Exception{

        NioEventLoopGroup group = new NioEventLoopGroup(1);
        ServerBootstrap b = new ServerBootstrap();
        b.group(group)
                .channel(NioServerSocketChannel.class)
                .localAddress(ConstantUtil.RS_CALC_RPC_PORT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new RSCalcServerHandler());
                    }
                });
        ChannelFuture f = b.bind().sync();
        f.channel().closeFuture().sync();
    }

    private static class RSCalcServerHandler extends ChannelInboundHandlerAdapter{
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof String){
                String fileName = (String) msg;
                String[] slaves = new String[2];
                PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
                slaves[0] = propertiesUtil.getValue("host.slave1");
                slaves[1] = propertiesUtil.getValue("host.slave2");
                for (String host : slaves){
                    createQuestFileClient(fileName, host, ctx);
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

        // 开启请求文件客户端
        private void createQuestFileClient(final String fileName, String host, final ChannelHandlerContext rpcCtx){
            Bootstrap b = new Bootstrap();
            b.group(rpcCtx.channel().eventLoop())
                    .channel(NioSocketChannel.class)
                    .remoteAddress(host, ConstantUtil.SEND_FILE_SERVICE_PORT)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new ClientFileQuestHandler(fileName, rpcCtx));
                        }
                    });
            b.connect();
        }
    }
}
