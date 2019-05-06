package com.linghang.service;

import com.linghang.core.Lagrange;
import com.linghang.proto.Block;
import com.linghang.proto.LagCalcRequestHeader;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class LagCalcService implements Service {

    private LagCalcRequestHeader header;
    private ChannelHandlerContext rpcCtx;

    public LagCalcService(LagCalcRequestHeader header, ChannelHandlerContext rpcCtx) {
        this.header = header;
        this.rpcCtx = rpcCtx;
    }

    @Override
    public void call() {

        if (header.isEncode()){
            Thread calcJobExecutor = new Thread(new LagCalcJob(header.getFileName(), header.isEncode(), rpcCtx));
            calcJobExecutor.setName(header.getFileName() + "-LagCalcJob");
            calcJobExecutor.start();
            System.out.println("======== " + calcJobExecutor.getName() + " START RUNNING ========");
        } else {

        }

    }

    private class LagCalcJob implements Runnable{
        private String fileName;
        private ChannelHandlerContext rpcCtx;
        private Lagrange lag;
        private RandomAccessFile rf;
        private byte[] buf;
        private boolean isEncode;

        public LagCalcJob(String fileName, boolean isEncode, ChannelHandlerContext rpcCtx) {
            this.fileName = fileName;
            this.rpcCtx = rpcCtx;
            this.isEncode = isEncode;
        }

        @Override
        public void run() {
            boolean calcSuccess;
            if (isEncode){
                // 进行 Lag 加密
                calcSuccess = doLagEncode();
            } else {
                // 进行 Lag 解密
                calcSuccess = doLagDecode();
            }

            // 计算完毕, 通知RPC调用方
            if (!calcSuccess){
                rpcCtx.writeAndFlush(ConstantUtil.SEND_ERROR_CODE);
            } else {
                rpcCtx.writeAndFlush(ConstantUtil.CALC_FINISH_CODE);
            }

            System.out.println("======== SERVER LAG CALCULATION JOB FOR FILE " + fileName + " FINISH ========");
        }

        // 边解码边发送到服务请求方
        private boolean doLagDecode(){
            boolean initSuccess = init();
            Block block = new Block();
            int start = 0;
            if (!initSuccess){
                System.err.println("========= LAG CALC SERVER INIT FAILED ========");
                return false;
            }

            int readByte;
            try {
                rf.seek(start);
                while((readByte = rf.read(buf)) != -1){
                    lag.encode(buf, readByte);

                    block.setBytes(buf);
                    block.setReadByte(readByte);
                    rpcCtx.writeAndFlush(block);    // send decode result

                    start += readByte;
                    rf.seek(start);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        private boolean doLagEncode(){
            boolean initSuccess = init();
            int start = 0;
            if (!initSuccess){
                System.err.println("========= LAG CALC SERVER INIT FAILED ========");
                return false;
            }

            int readByte;
            try {
                rf.seek(start);
                while((readByte = rf.read(buf)) != -1){
                    lag.encode(buf, readByte);
                    rf.write(buf, 0, buf.length);
                    start += readByte;
                    rf.seek(start);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        private boolean init(){

            // init lagrange calculator
            int[] x = new int[]{1, 2, -3};
            int[] alpha = new int[]{-6, 5, 4};
            lag = new Lagrange(alpha, x);

            // init file
            PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
            String path = propertiesUtil.getValue("service.local_redundant_save_path");
            File file = new File(path + Util.geneRedundancyName(fileName));
            try {
                rf = new RandomAccessFile(file, "rw");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            // init calc buf
            int bufSize = Util.getBufSize(file.length(), ConstantUtil._1K, ConstantUtil._5K);
            bufSize = bufSize == -1? 3 : bufSize;
            buf = new byte[bufSize];

            return true;
        }
    }

    public static void main(String[] args) {
        PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
        String path = propertiesUtil.getValue("service.local_redundant_save_path");
        File file = new File(path + Util.geneRedundancyName("1M.pdf"));

        int bufSize = Util.getBufSize(file.length(), ConstantUtil._3K, ConstantUtil._5K);
        System.out.println("Buf size = " + bufSize);

        int readByte;
        int start = 0;
        byte[] buf = new byte[bufSize];
        try {
            RandomAccessFile rf = new RandomAccessFile(file, "r");
            rf.seek(start);
            while((readByte = rf.read(buf)) != -1) {
                System.out.println("RF read " + readByte + " bytes.");
                start += readByte;
                rf.seek(start);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
