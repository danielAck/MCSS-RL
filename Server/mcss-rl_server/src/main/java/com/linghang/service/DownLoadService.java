package com.linghang.service;

import com.linghang.proto.Block;
import com.linghang.proto.GetBlockHeader;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;
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


import java.io.RandomAccessFile;
import java.net.InetAddress;

public class DownLoadService implements Service{

    private String fileName;
    private String[] hosts;
    private String redundantBlockRecvHost;
    private Service rsCalcService;

    public DownLoadService(String fileName, String[] hosts, String redundantBlockRecvHost) {
        this.fileName = fileName;
        this.hosts = hosts;
        this.redundantBlockRecvHost = redundantBlockRecvHost;
        initRSCalcService();
    }

    private void initRSCalcService(){
        String remoteFileName = Util.geneTempName(fileName);
        String remoteFilePath = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME).getValue("service.lag_decode_temp_path");
        this.rsCalcService = new RSCalcServiceFactory(remoteFileName, remoteFilePath, hosts, redundantBlockRecvHost).createService();
    }

    @Override
    public void call() {
        String[] downloadHosts = getDownloadHosts(hosts);
        getBlock(downloadHosts);

        if (checkNeedRSCalc(hosts)){
            rsCalcService.call();
        }
    }

    // TODO: 利用数据库，判断选择的结点中是否含有冗余块存储结点
    private boolean checkNeedRSCalc(String[] hosts){
        for (String host : hosts){
            if (host.equals("127.0.0.1")){
                return true;
            }
        }
        return false;
    }

    // get block from hosts
    private void getBlock(String[] hosts){
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        for (String host : hosts){
            createGetBlockClient(host, fileName, group);
        }
    }

    private String[] getDownloadHosts(String[] hosts){
        // TODO: 从数据库中依次判断hosts中只需直接下载的结点

        return new String[]{"192.168.31.120", "192.168.31.121"};
    }

    private void createGetBlockClient(final String host, String fileName, NioEventLoopGroup group){

        long startPos = 0;
        long length = -1;
        PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
        String remoteFileName = Util.genePartName(fileName);
        String remoteFilePath = propertiesUtil.getValue("service.local_part_save_path");
        final GetBlockHeader getBlockHeader = new GetBlockHeader(remoteFileName, remoteFilePath, startPos, length);

        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(host, ConstantUtil.GET_DATA_SERVICE_PORT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new ObjectEncoder())
                                .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers
                                        .weakCachingConcurrentResolver(null)))
                                .addLast(new GetBlockHandler(getBlockHeader, host));
                    }
                });
        b.connect();
    }

    // TODO：从数据库中根据IP获取需要下载文件对应的起始下标
    private long getStartPos(String host){


        return 0;
    }

    private class GetBlockHandler extends ChannelInboundHandlerAdapter{

        private PropertiesUtil util;
        private GetBlockHeader header;
        private String host;
        private byte[] buf;
        private long start;
        private RandomAccessFile rf;

        public GetBlockHandler(GetBlockHeader header, String host) {
            util = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
            this.host = host;
            this.header = header;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            boolean initSuccess = initWrite(host);
            if (!initSuccess){
                ctx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
            } else {
                ctx.writeAndFlush(header);
                System.out.println("======== NAME NODE BEGIN TO QUEST BLOCK FROM " +
                        ctx.channel().remoteAddress().toString() + " FOR FILE " + fileName + " ========");
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof Block){
                Block block = (Block) msg;
                buf = block.getBytes();
                rf.write(buf, 0, block.getReadByte());
                start += block.getReadByte();
                ctx.writeAndFlush(start);
            }
            else if (msg instanceof Integer){
                Integer resCode = (Integer) msg;
                if (resCode.equals(ConstantUtil.SEND_FINISH_CODE)){
                    System.out.println("======== GET BLOCK FROM " +
                            ctx.channel().remoteAddress().toString() + " DATA FINISH =========");
                } else if (resCode.equals(ConstantUtil.SEND_ERROR_CODE)){
                    System.err.println("======== GET BLOCK FROM " +
                            ctx.channel().remoteAddress().toString() + " DATA FAILED =========");
                }
                ctx.close();
                ctx.channel().eventLoop().shutdownGracefully();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

        // 初始化保存文件
        private boolean initWrite(String host){
            String path = util.getValue("service.local_download_path");
            start = getStartPos(host);
            try {
                rf = new RandomAccessFile(path + fileName, "rw");
                rf.seek(start);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }
}
