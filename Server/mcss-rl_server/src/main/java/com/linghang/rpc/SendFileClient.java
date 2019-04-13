package com.linghang.rpc;

import com.linghang.pojo.SendFileJobDescription;
import com.linghang.pojo.SendPosition;
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

    private SendFileJobDescription jobDescription;

    public SendFileClient(SendFileJobDescription jobDescription) {
        this.jobDescription = jobDescription;
    }

    public static void main(String[] args) throws Exception {

        String filePath = "F:\\WUST\\program\\dsz\\";
        String fileName = "1M.pdf";
        File file = new File(filePath + fileName);
        int startPos = 0;
        long endPos = 10249;

        SendPosition sendPosition = new SendPosition(startPos, endPos);

        // create job description
        SendFileJobDescription jobDescription = new SendFileJobDescription(filePath, fileName, sendPosition, "127.0.0.1", 9999);

        // new Client

        SendFileClient sendClient = new SendFileClient(jobDescription);
        sendClient.start();
        System.out.println("======== CLIENT SEND FINISH =========");
    }

    public void start() throws Exception{

        NioEventLoopGroup group = new NioEventLoopGroup(1);
        try{
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(jobDescription.getHost(), jobDescription.getPort())
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
