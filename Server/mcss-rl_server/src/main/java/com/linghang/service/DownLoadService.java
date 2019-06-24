package com.linghang.service;

import com.linghang.dao.UploadFileManageable;
import com.linghang.dao.impl.UploadFileManageImpl;
import com.linghang.pojo.UploadFile;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DownLoadService implements Service{

    private String fileName;
    private String fileSavePath;
    private String[] hosts;
    private String redundantBlockRecvHost;
    private Service rsCalcService;

    public DownLoadService(String fileName, String fileSavePath, String[] hosts, String redundantBlockRecvHost) {
        this.fileName = fileName;
        this.fileSavePath = fileSavePath;
        this.hosts = hosts;
        this.redundantBlockRecvHost = redundantBlockRecvHost;
        initRSCalcService();
    }

    private void initRSCalcService(){
        String calcFileName = Util.geneTempName(fileName);
        String calcFilePath = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME).getValue("service.lag_decode_temp_path");
        String redundancySaveFileName = fileName;
        String redundancySaveFilePath = fileSavePath;
        this.rsCalcService = new RSCalcServiceFactory(calcFileName, calcFilePath, redundancySaveFileName, redundancySaveFilePath,
                hosts, redundantBlockRecvHost, true).createService();
    }

    @Override
    public void call() {
        Object[] downloadHosts = getDownloadHosts(hosts);
        getBlock(downloadHosts);

        if (checkNeedRSCalc(hosts)){
            rsCalcService.call();
        }
    }

    private boolean checkNeedRSCalc(String[] hosts){
        UploadFileManageable uploadFileService = new UploadFileManageImpl();
        String redundantHost = uploadFileService.getRedundantHostByFileName(Util.getFileUploadName(fileName));
        return Arrays.asList(hosts).contains(redundantHost);
    }

    // get block from hosts
    private void getBlock(Object[] hosts){
        Thread getBlockJob = new Thread(new GetBlockJob(hosts, fileName));
        getBlockJob.setName(fileName + "-get_block-job");
        getBlockJob.start();
    }

    private Object[] getDownloadHosts(String[] hosts){

        UploadFileManageable uploadFileService = new UploadFileManageImpl();
        String redundantHost = uploadFileService.getRedundantHostByFileName(Util.getFileUploadName(fileName));
        List<String> temp = new ArrayList<>(Arrays.asList(hosts));
        if (!temp.contains(redundantHost)){
            return hosts;
        } else {
            temp.remove(redundantHost);
            return temp.toArray();
        }
    }

    private class GetBlockJob implements Runnable{

        private Object[] hosts;
        private String fileName;
        private NioEventLoopGroup group;
        private CountDownLatch getBlockFinishCdl;

        public GetBlockJob(Object[] hosts, String fileName) {
            this.hosts = hosts;
            this.fileName = fileName;
            this.group = new NioEventLoopGroup(1);
            this.getBlockFinishCdl = new CountDownLatch(hosts.length);
        }

        @Override
        public void run() {
            for (Object host : hosts){
                createGetBlockClient((String) host, fileName, group, getBlockFinishCdl);
            }
            try {
                getBlockFinishCdl.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("========= GET BLOCK JOB FINISH =========");
            group.shutdownGracefully();
        }
    }

    private void createGetBlockClient(final String host, String fileName, NioEventLoopGroup group, final CountDownLatch getBlockFinishCdl){

        long startPos = 0;
        long length = -1;
        PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
        String remoteFileName = Util.geneTempName(fileName);
        String remoteFilePath = propertiesUtil.getValue("service.lag_decode_temp_path");
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
                                .addLast(new GetBlockHandler(getBlockHeader, host, getBlockFinishCdl));
                    }
                });
        b.connect();
    }

    private long getStartPos(String host){

        UploadFileManageable uploadFileService = new UploadFileManageImpl();
        Integer cloudId = uploadFileService.getCloudIdByFileNameAndHost(Util.getFileUploadName(fileName), host);
        UploadFile file = uploadFileService.getUploadFileByFileName(Util.getFileUploadName(fileName));
        return file.getLength()/3 * cloudId;
    }

    private class GetBlockHandler extends ChannelInboundHandlerAdapter{

        private PropertiesUtil util;
        private GetBlockHeader header;
        private String host;
        private byte[] buf;
        private long start;
        private long flag;
        private RandomAccessFile rf;
        private CountDownLatch waitGetBlockFinishCdl;

        public GetBlockHandler(GetBlockHeader header, String host, CountDownLatch waitGetBlockFinishCdl) {
            util = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
            this.host = host;
            this.header = header;
            this.waitGetBlockFinishCdl = waitGetBlockFinishCdl;
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
                ctx.writeAndFlush(start - flag);
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
                waitGetBlockFinishCdl.countDown();
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
            flag = start;
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
