package com.linghang.io;

import com.linghang.rpc.client.ClientFileQuestHandler;
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
    private byte[] readBuf;
    private RandomAccessFile tempReadRF;
    private RandomAccessFile writeRF;
    private PropertiesUtil propertiesUtil;

    public FileWriter(String fileName) {
        propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
        readBuf = new byte[bufLength];
        questFileName = fileName;
        initReadFile();
        initWriteFile();
        initFlg();
    }

    private void initFlg(){
        fileReadFlg = ClientFileQuestHandler.fileReadFlg;
        if (fileReadFlg.get(questFileName) == null){
            fileReadFlg.put(questFileName, ConstantUtil.FILE_READ_INIT_FLG);
        }
    }

    // 将接收到的文件块存储在 temp 目录下
    private void initReadFile() {
        String readFileName = Util.genePartName(questFileName);

        String path = propertiesUtil.getValue("service.local_part_save_path");
        File file = new File(path + readFileName);
        try {
            tempReadRF = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            System.out.println("======= SEGMENT PART DOESN'T EXIT IN SERVER =======");
            e.printStackTrace();
        }
    }

    // 将计算好的结果写到 redundant 目录下
    private void initWriteFile() {
        String saveFileName = Util.geneRedundancyName(questFileName);
        String path = propertiesUtil.getValue("service.local_redundant_save_path");
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void write(long start, byte[] msg, int writeLength) throws Exception{

        Long flg = fileReadFlg.get(questFileName);

        // 单线程不用考虑并发
        if (start > flg){
            read(tempReadRF, start);
        } else {
            read(writeRF, start);
        }

        add(msg, writeLength);

        writeRF.seek(start);
        writeRF.write(readBuf, 0, writeLength);
        fileReadFlg.put(questFileName, start + writeLength);
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
