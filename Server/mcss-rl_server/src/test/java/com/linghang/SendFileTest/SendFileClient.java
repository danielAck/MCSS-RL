package com.linghang.SendFileTest;

import com.linghang.pojo.Job;
import com.linghang.pojo.SendFileJobDescription;
import com.linghang.pojo.SendFileJobFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;

public class SendFileClient {

    private String host;
    private int port;
    private SendFileJobDescription jobDescription;

    public SendFileClient(String host, int port, SendFileJobDescription jobDescription) {
        this.host = host;
        this.port = port;
        this.jobDescription = jobDescription;
    }

    public static void main(String[] args) throws Exception {
        String filePath = "F:\\WUST\\program\\dsz\\";
        String fileName = "1M.pdf";
        File file = new File(filePath + fileName);
        int startPos = 0;
        long endPos = file.length();

        SendFileJobDescription jobDescription = new SendFileJobDescription(filePath, fileName, startPos, endPos);

        // TODO: 完成 SendFileJob 的生成部分

        SendFileClient client = new SendFileClient("127.0.0.1",
                9999, jobDescription);
        client.start();
    }

    public void start() throws Exception{


        NioEventLoopGroup group = new NioEventLoopGroup(1);
        try{
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(host, port)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new ObjectEncoder())
                                    .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers
                                            .weakCachingConcurrentResolver(null)))
                                    .addLast(new ClientSendHandler(jobDescription));
                        }
                    });
            ChannelFuture f = b.connect().sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
