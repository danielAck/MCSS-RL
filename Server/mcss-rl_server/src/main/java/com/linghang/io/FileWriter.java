package com.linghang.io;

import com.linghang.rpc.client.handler.ClientRSCalcHandler;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ConcurrentHashMap;

public class FileWriter {

    private static final int bufLength = ConstantUtil.BUFLENGTH;
    private static ConcurrentHashMap<String, Long> fileReadFlg;
    private String questFileName;
    private String questFilePath;
    private byte[] readBuf;
    private long originStartPos;
    private RandomAccessFile localReadRF;
    private RandomAccessFile writeRF;
    private PropertiesUtil propertiesUtil;

    public FileWriter(String fileName, String filePath, long originStartPos) {
        this.propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
        this.readBuf = new byte[bufLength];
        this.questFileName = fileName;
        this.questFilePath = filePath;
        this.originStartPos = originStartPos;
        initLocalReadFile();
        initWriteFile();
        initFlg();
    }

    public boolean closeRF(){
        boolean res = true;
        try {
            localReadRF.close();
            writeRF.close();
        } catch (IOException e) {
            res = false;
            e.printStackTrace();
        }
        return res;
    }

    private void initFlg(){
        fileReadFlg = ClientRSCalcHandler.fileReadFlg;
        if (fileReadFlg.get(questFileName) == null){
            fileReadFlg.put(questFileName, ConstantUtil.FILE_READ_INIT_FLG);
        }
    }

    // 读取本地存储的分块文件内容
    private void initLocalReadFile() {
        File file = new File(questFilePath + questFileName);
        try {
            localReadRF = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            System.out.println("======= SEGMENT PART DOESN'T EXIT IN SERVER =======");
            e.printStackTrace();
        }
    }

    // 将计算好的结果写到 calctemp 目录下
    private void initWriteFile() {
        String saveFileName = questFileName;
        String path = propertiesUtil.getValue("service.calc_temp_save_path");
        File file = new File(path + saveFileName);
        if (!file.exists()){
            try {
                boolean res = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            writeRF = new RandomAccessFile(file, "rw");
            writeRF.seek(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(long start, byte[] msg, int writeLength) throws Exception{

        // flg = 下一个需要新写入的字节
        Long flg = fileReadFlg.get(questFileName);

        // 单线程不用考虑并发
        if (start >= flg){
            System.out.println("======== READ FROM LOCAL FILE ========");
            read(localReadRF, start);

            // 更新读取下标
            fileReadFlg.put(questFileName, start + writeLength);
        } else {
            System.out.println("======== READ FROM WRITE FILE ========");
            read(writeRF, (start - originStartPos));
        }

        add(msg, writeLength);

        writeRF.seek(start - originStartPos);
        writeRF.write(readBuf, 0, writeLength);
    }

    private void read(RandomAccessFile rf, long start) throws Exception{
        rf.seek(start);
        rf.read(readBuf);
    }

    private void add(byte[] msg, int writeLength) throws IOException{
        for (int i = 0; i < writeLength; i++){
            readBuf[i] = (byte)(readBuf[i] ^ msg[i]);
        }
    }

}
