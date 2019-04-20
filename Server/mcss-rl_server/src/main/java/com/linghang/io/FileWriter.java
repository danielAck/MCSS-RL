package com.linghang.io;

import com.linghang.rpc.client.ClientFileQuestHandler;
import com.linghang.util.ConstantUtil;
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
    private RandomAccessFile localReadRF;
    private RandomAccessFile writeRF;

    public FileWriter(String fileName) {
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

    private void initReadFile() {
        String readFileName = Util.genePartName(questFileName);
        File file = new File(ConstantUtil.CLIENT_PART_SAVE_PATH + readFileName);
        try {
            localReadRF = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            System.out.println("======= SEGMENT PART DOESN'T EXIT IN SERVER =======");
            e.printStackTrace();
        }
    }

    private void initWriteFile() {
        String saveFileName = Util.geneRedundancyName(questFileName);
        File file = new File(ConstantUtil.CLIENT_REDUNDANCY_SAVE_PATH + saveFileName);
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
            read(localReadRF, start);
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

    public void calculateTest(){
        byte a = -16;
        byte b = -2;
        byte c = (byte) (a ^ b);
        System.out.println(c);
        c = (byte) (c ^ -2);
        System.out.println(c);
    }

    public static void main(String[] args) throws Exception {
//        FileWriter fileWriter = new FileWriter("2K.txt");
        System.out.println(Util.geneRedundancyName("2K.txt"));
    }

}
