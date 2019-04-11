package com.linghang.CalculateTest;

import com.linghang.util.ConstantUtil;
import com.linghang.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileWriter {

    private static final int bufLength = ConstantUtil.BUFLENGTH;
    private String questFileName;
    private byte[] readBuf;
    private RandomAccessFile readRF;
    private RandomAccessFile writeRF;

    public FileWriter(){}

    public FileWriter(String fileName) {
        readBuf = new byte[bufLength];
        questFileName = fileName;
        initReadFile();
        initWriteFile();
    }

    private void initReadFile() {
        String readFileName = Util.genePartName(questFileName);
        File file = new File(ConstantUtil.CLIENT_PART_SAVE_PATH + readFileName);
        try {
            readRF = new RandomAccessFile(file, "r");
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
        read(start);
        add(msg, writeLength);

        writeRF.seek(start);
        writeRF.write(readBuf, 0, writeLength);
    }

    private void read(long start) throws Exception{
        readRF.seek(start);
        readRF.read(readBuf);
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
