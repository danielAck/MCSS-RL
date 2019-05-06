package com.linghang.core;

import com.linghang.util.CalcUtil;

import java.util.Arrays;

public class Lagrange {

    private static int[] gfilog;
    private static int[] gflog;
    private static final int AND_BASE_8 = CalcUtil.getAndBase8();
    private static final int MOD_BASE = CalcUtil.getModBase();
    private  int[] alpha;
    private  int[] x;

    public Lagrange(int[] alpha, int[] x) {
        CalcUtil calcUtil = new CalcUtil();
        gfilog = calcUtil.getGFILog();
        gflog = calcUtil.getGFLog();
        this.alpha = alpha;
        this.x = x;
    }

    /**
     * Lagrange interpolation
     * @param d data
     * @param off length of data needed to be encoded
     */
    public void encode(byte[] d, int off){
        this.encode(alpha, x, d, off);
    }

    /**
     * Decode data
     * @param d data
     * @param off length of data needed to be decoded
     */
    public void decode(byte[] d, int off){
        this.encode(x, alpha, d, off);
    }

    private void encode(int[] alpha, int[] x, byte[] y, int off){

        // 将需要进行计算的数据转为无符号型
        convertUnsigned(x);
        convertUnsigned(alpha);

        if (y.length % 3 != 0){
            System.err.println("======== The length of the data to be encoded needs to be a multiple of 3 ========");
            return;
        }

        int[] temp = new int[3];
        for (int m = 0; m < off; m+=3){

            // 计算三个数据
            for (int i = 0; i < 3; i++){
                int res = 0;
                for (int j = 0; j < 3; j++){
                    int t = 1;
                    for (int k = 0; k < 3; k++){
                        if (k != j){
                            t = gfilog[(gflog[t] + gflog[gfilog[(gflog[alpha[i]^x[k]] + (MOD_BASE - gflog[(x[j]^x[k])])) % MOD_BASE]]) % MOD_BASE];   // t = t * (alpha[i] - x[k])/(x[j] - x[k]);
                        }
                    }
                    t = gfilog[(gflog[t] + gflog[y[m+j] & AND_BASE_8]) % MOD_BASE];     // t = g * y[m+j]
                    res = res ^ t;     // res = res + t
                }
                temp[i] = res;
            }

            // 赋值计算结果
            for (int i = 0; i < 3; i++){
                y[m+i] = (byte) temp[i];
            }
        }

    }

    private void convertUnsigned(int[] data){
        for (int i = 0; i < data.length; i++){
            data[i] = data[i] & AND_BASE_8;
        }
    }

    public static void main(String[] args) throws Exception{

        CalcUtil calcUtil = new CalcUtil();
        gflog = calcUtil.getGFLog();
        gfilog = calcUtil.getGFILog();

        int[] x = {1, 2, -3};
        int[] alpha = {-6, 5, 4};
        byte[] y = {-105, 79, 84, 33, -90, 45, 23, 11, -8};

        Lagrange lag = new Lagrange(x, alpha);
        System.out.println("Before encode : ");
        System.out.println(Arrays.toString(y));

        lag.encode(y, y.length);
        System.out.println("After encode : ");
        System.out.println(Arrays.toString(y));

        lag.encode(y, y.length);
        System.out.println("After encode : ");
        System.out.println(Arrays.toString(y));
    }

}
