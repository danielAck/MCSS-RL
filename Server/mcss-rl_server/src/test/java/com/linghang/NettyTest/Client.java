package com.linghang.NettyTest;

import com.linghang.io.BlockDetail;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;
import java.util.Date;

public class Client {

    private String ip;
    private int port;

    public Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public static void main(String[] args) throws Exception{
        String filePath = "F:\\WUST\\program\\dsz\\";
        String fileName = "1M.pdf";
        Client client = new Client("127.0.0.1", 9999);
        client.start(filePath, fileName);
    }

    private BlockDetail setBlockDetail(String filePath, String fileName) {
        File file = new File(filePath + fileName);
        BlockDetail blockDetail = new BlockDetail();
        blockDetail.setFileName("1M.pdf");
        blockDetail.setStartPos(0);
        return blockDetail;
    }

    public void start(String filePath, String fileName) throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        final BlockDetail blockDetail = this.setBlockDetail(filePath, fileName);
            try{
                Bootstrap b = new Bootstrap();
                b.group(group)
                        .channel(NioSocketChannel.class)
                        .remoteAddress(ip, port)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline()
                                        .addLast(new ObjectEncoder())
                                        .addLast(new ObjectDecoder(ClassResolvers
                                                .weakCachingConcurrentResolver(null)))
                                        .addLast(new InboundHandler1(blockDetail));
                            }
                        });
                ChannelFuture f = b.connect().sync();
            System.out.println("Client connect successfully! " + new Date());
            f.channel().closeFuture().sync();
            System.out.println("Client closed connection! " + new Date());
        }
        finally {
            group.shutdownGracefully();
        }
    }

}
