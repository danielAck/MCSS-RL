package com.linghang.NIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class Data {

    private String dataPath;
    private long fileSize;
    private long col_size;
    private double buff_size;

    public Data(String dataPath) throws IOException {
        this.dataPath = dataPath;
        this.fileSize = getFileSize(dataPath);
        this.col_size = getColSize(fileSize);
        this.buff_size = getBufSize();
    }

    public String getDataPath() {
        return dataPath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getCol_size() {
        return col_size;
    }

    public double getBuff_size() {
        return buff_size;
    }

    private double getBufSize () throws IOException{

        long col_size = this.col_size;
        double _16M = 16.0 * 1024 * 1024;
        double _2M = 2.0 * 1024 * 1024;

        long lowerBound = (long)Math.ceil(_2M / 9);
        long upperBound = (long)Math.ceil(_16M / 9);
        long mid = (long)Math.floor((lowerBound + upperBound) / 2.0);

        long startTime = System.currentTimeMillis();
        double bufSize = getBufSize(mid, col_size, lowerBound);
        long endTime = System.currentTimeMillis();

        if (bufSize != -1){
            System.out.println("Buffer size = " + (bufSize * 3 * 3) / (1024*1024) + "M");
            System.out.println("Block num = " + (col_size / (bufSize*3)));
            System.out.println("Run Time = " + (endTime - startTime) + "ms");
            return bufSize;
        } else {
            System.out.println("Can not find proper buffer size!");
            return -1;
        }
    }

    private double getBufSize(long mid, long col_size, long lowerBound){
        for (long i = 0; i <= (mid - lowerBound); i++){
            long low = mid - i;
            long upper = mid + i;
            long temp = col_size / 3;
            if (temp % low == 0)
                return low;
            if (temp % upper == 0)
                return upper;
        }
        return -1;
    }

    private long getFileSize(String path) throws IOException {
        File file = new File(path);
        if (file.exists() && file.isFile()){
            FileInputStream is = new FileInputStream(file);
            FileChannel fileChannel = is.getChannel();
            return fileChannel.size();
        } else {
            System.out.println("File doesn't exist!");
            return 0;
        }
    }

    private long getColSize(long file_size){
        if(file_size % 9 == 0){
            return  (file_size / 9) * 3;
        } else {
            return  (long)(Math.floor(file_size / 9.0) + 1) * 3;
        }
    }


}
