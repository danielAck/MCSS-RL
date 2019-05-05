package com.linghang.core;

import com.linghang.util.CalcUtil;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Lagrange {

    private static int[] gfilog;
    private static int[] gflog;
    private static final int AND_BASE_8 = CalcUtil.getAndBase8();
    private static final int MOD_BASE = CalcUtil.getModBase();

    public Lagrange() {
        CalcUtil calcUtil = new CalcUtil();
        gfilog = calcUtil.getGFILog();
        gflog = calcUtil.getGFLog();
    }

    /**
     * 进行Lagrange插值法的计算
     *
     * @param x 提前指定好的自变量 (需要已经处理为无符号）
     * @param alpha 带入Lagrange函数的值 (需要已经处理为无符号）
     * @param y 需要隐藏的数据
     */
    public void encode(int[] x, int[] alpha, byte[] y){

        // 将需要进行计算的数据转为无符号型
        convertUnsigned(x);
        convertUnsigned(alpha);

        if (y.length % 3 != 0){
            System.err.println("======== The length of the data to be encoded needs to be a multiple of 3 ========");
        }

        int[] temp = new int[3];
        for (int m = 0; m < y.length; m+=3){

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

    private byte mul(byte a, byte b){
        return (byte) gfilog[gflog[a] ^ gflog[b]];
    }

    private byte dev(byte a, byte b){
        return (byte) gfilog[gflog[a] ^ gflog[b]];
    }

    public static void main(String[] args) throws Exception{

        CalcUtil calcUtil = new CalcUtil();
        gflog = calcUtil.getGFLog();
        gfilog = calcUtil.getGFILog();

        int[] x = {1, 2, -3};
        int[] alpha = {-6, 5, 4};
        byte[] y = {-105, 79, 84, 33, -90, 45, 23, 11, -8};

        Lagrange lag = new Lagrange();
        System.out.println("Before encode : ");
        System.out.println(Arrays.toString(y));

        lag.encode(x, alpha, y);
        System.out.println("After encode : ");
        System.out.println(Arrays.toString(y));

        lag.encode(alpha, x, y);
        System.out.println("After encode : ");
        System.out.println(Arrays.toString(y));
    }

}
