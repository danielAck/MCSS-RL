package com.linghang.rpc.server;

import com.linghang.util.ConstantUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class RSCalcServer {


    public RSCalcServer() {
    }

    public static void main(String[] args) {

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
                NioEventLoopGroup clientGroup = new NioEventLoopGroup(1);

            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

        // 开启请求文件客户端
        private void start(NioEventLoopGroup group){

        }

    }

}
