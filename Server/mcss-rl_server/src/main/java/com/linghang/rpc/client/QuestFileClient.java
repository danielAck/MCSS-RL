package com.linghang.rpc.client;

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

public class QuestFileClient {

    private NioEventLoopGroup group;
    private String ip;
    private int port;
    private String questFileName;

    public QuestFileClient(String ip, int port, NioEventLoopGroup group) {
        this.ip = ip;
        this.port = port;
        this.group = group;
        this.questFileName = null;
    }

    public void setQuestFileName(String questFileName) {
        this.questFileName = questFileName;
    }

    public void resetClientConf(String ip, int port, String questFileName){
        this.ip = ip;
        this.port = port;
        this.questFileName = questFileName;
    }

    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 9999;
        String questFileName = "1M.pdf";
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        QuestFileClient client = new QuestFileClient(host, port, group);
        client.setQuestFileName(questFileName);
        client.start();
    }

    public void start() throws Exception{

        if (questFileName == null){
            System.out.println("======= Error : Please set quest file name first! =======");
            return;
        }

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
                                    .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers
                                            .weakCachingConcurrentResolver(null)))
                                    .addLast(new ClientFileQuestHandler(questFileName));
                        }
                    });
            ChannelFuture f = b.connect().sync();
            f.channel().closeFuture().sync();
        }
        finally {
            group.shutdownGracefully();
        }
    }

}
