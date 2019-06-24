package com.linghang.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class Util {

    public static int bytes2Int(byte[] bytes){
        int index = 0;
        return (bytes[index] & 0xff) << 24 | (bytes[index+1] & 0xff) << 16 | (bytes[index+2] & 0xff) << 8 | bytes[index+3] & 0xff;
    }

    public static byte[] intToBytes2(int n){
        byte[] b = new byte[4];

        for(int i = 0;i < 4;i++)
        {
            b[i]=(byte)(n>>(24-i*8));

        }
        return b;
    }

    public static int[][] getDataFromFile(String path) throws IOException {

        File file = new File(path);
        int word;
        int row = 0;
        int col = 0;
        int colSize = 0;
        long fileByteLength = 0;

        // 判断文件是否存在
        if (file.exists() && file.isFile()){

            // 获得文件字节长度
            FileInputStream is = new FileInputStream(path);
            FileChannel fileChannel = is.getChannel();
            fileByteLength = fileChannel.size();
            colSize = getColSize(fileByteLength);

            int[][] res = new int[3][colSize];

            ByteBuffer buffer = ByteBuffer.allocate(4096);
            while((word = fileChannel.read(buffer)) != -1){
                buffer.flip();
                if (buffer.hasArray()){
                    byte[] bytes = buffer.array();
                    for (int i = 0; i < buffer.limit(); i++){
                        res[row][col] = bytes[i];
                        col++;
                        if (col == colSize){
                            col = 0;
                            row++;
                        }
                    }
                }
            }
            is.close();
            fileChannel.close();
            return res;
        } else {
            System.out.println("文件不存在或非文件！");
            return null;
        }
    }

    public static byte[] singleRowTest(String path) throws IOException{
        File file = new File(path);
        int word;
        int row = 0;
        int col = 0;
        int colSize = 0;
        long fileByteLength = 0;

        // 判断文件是否存在
        if (file.exists() && file.isFile()){

            // 获得文件字节长度
            FileInputStream is = new FileInputStream(path);
            FileChannel fileChannel = is.getChannel();
            fileByteLength = fileChannel.size();
            colSize = getColSize(fileByteLength);

            byte[] res = new byte[3*colSize];

            ByteBuffer buffer = ByteBuffer.allocate(4096);
            while((word = fileChannel.read(buffer)) != -1){
                buffer.flip();
                if (buffer.hasArray()){
                    byte[] bytes = buffer.array();
                    for (int i = 0; i < buffer.limit(); i++){
                        res[row*colSize+col] = bytes[i];
                        col++;
                        if (col == colSize){
                            col = 0;
                            row++;
                        }
                    }
                }
            }
            is.close();
            fileChannel.close();
            return res;
        } else {
            System.out.println("文件不存在或非文件！");
            return null;
        }
    }

    public static int getColSize(long fileByteLength){
        if(fileByteLength % 9 == 0){
            return (int) (fileByteLength / 9) * 3;
        } else {
            return (int)(Math.floor(fileByteLength / 9.0) + 1) * 3;
        }
    }

    public static void printByte1DArray(byte[] res, int colSize){
        for (int i = 0; i < res.length; i++) {
            System.out.print(res[i]);
            if ((i+1)%colSize == 0)
                System.out.println();
            else
                System.out.print(", ");
        }
    }

    /**
     * 随机分配数据发送到的服务器
     * partition[i] = a means 下标为 i 的数据发送到下标为 a 的服务器
     * @param dataNum 数据个数
     * @param serverNum 服务器个数
     * @return generated array
     */
    public static int[] generateRandomPartition(int dataNum, int serverNum){

        int serverReceiveNum = dataNum / serverNum;
        int[] partition = new int[dataNum];

        // 生成数组
        int currentServerIndex = 0;
        int changeTime = 0;
        for(int i = 0; i < dataNum; i++){
            if (i % serverReceiveNum == 0 && (changeTime < serverNum)){
                currentServerIndex++;
                changeTime++;
            }
            partition[i] = currentServerIndex;
        }

        // 随机打乱
        Random random = new Random();
        for(int i=0; i < dataNum; i++){
            int p = random.nextInt(i+1);
            int tmp = partition[i];
            partition[i] = partition[p];
            partition[p] = tmp;
        }

        return partition;
    }

    /**
     * 根据文件大小获取所需大小区间的合适的缓冲区大小
     * @param fileSize 文件大小
     * @param min 所需区间下限
     * @param max 所需区间上限
     * @return 缓冲区大小
     */
    public static int getBufSize(long fileSize, int min, int max){

        if (fileSize < min)
            return -1;

        int lowerBound = (int)Math.ceil(min / 3.0);
        int upperBound = (int)Math.ceil(max / 3.0);
        int mid = (int)Math.floor((lowerBound + upperBound) / 2.0);

        for (int i = 0; i <= (mid - lowerBound); i++){
            int low = mid - i;
            int upper = mid + i;
            if ((fileSize % low)%3 == 0)
                return low*3;
            if ((fileSize % upper)%3 == 0)
                return upper*3;
        }

        return -1;
    }

    /**
     * 通过 connectID 获取需要连接的 DataNode 地址
     * @param connectID
     * @return
     */
    public static String getInetAddrByConnectID (String connectID){
        PropertiesUtil propertiesUtil = new PropertiesUtil("node.properties");
        return propertiesUtil.getValue(connectID);
    }

    /**
     * 生成存储文件名
     * 格式：fileName.part
     * @param fileName 待处理的文件名
     * @return 存储文件名
     */
    public static String geneRedundancyName(String fileName){
        String splitName = fileName.split("\\.")[0];
        return splitName + ".redundancy";
    }

    public static String genePartName(String fileName){
        String splitName = fileName.split("\\.")[0];
        return splitName + ".part";
    }

    public static String geneTempName(String fileName){
        String splitName = fileName.split("\\.")[0];
        return splitName + ".temp";
    }

    public static String getFileUploadName(String fileName){
        return fileName.split("\\.")[0];
    }

    public static String getFileSubfix(String fileName){
        return fileName.split("\\.")[1];
    }

    public static void main(String[] args) {
        PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
        String path = propertiesUtil.getValue("service.local_redundant_save_path");
        File file = new File(path + Util.geneRedundancyName("1M.pdf"));
        System.out.println(getBufSize(file.length(), ConstantUtil._3K, ConstantUtil._5K));
    }
}
