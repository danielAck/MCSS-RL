package com.linghang.core;

import com.linghang.util.CalcUitl;
import com.linghang.util.ConstantUtil;
import io.netty.util.Constant;

public class Lagrange {

    private static int[] gfilog;
    private static int[] gflog;

    public Lagrange() {
        CalcUitl calcUitl = new CalcUitl();
        gfilog = calcUitl.getGfilog();
        gflog = calcUitl.getGflog();
    }

    /**
     * 进行Lagrange插值法的计算
     *
     * @param beta 承载隐藏后的结果
     * @param x 提前指定好的自变量
     * @param alpha 带入Lagrange函数的值
     * @param y 需要隐藏的数据
     */
    public void encode(byte[] beta, byte[] x, byte[] alpha, byte[] y){

//        for(int i = 0; i < 3; i++){
//            double res = 0;
//            for(int j = 0; j < 3; j++){
//                double temp = 1;
//                for(int k = 0; k < 3; k++){
//                    if(k != j) {
//                        temp = temp * (alpha[i] - x[k])/(x[j] - x[k]);
//                    }
//                }
//                temp = temp * y[j];
//                res = res + temp;
//            }
//            beta[i] = (int)Math.round(res);
//        }
    }

    private byte mul(byte a, byte b){
        return (byte) gfilog[(gflog[a] + gflog[b]) % (2^8 - 1)];
    }

    private byte dev(byte a, byte b){
        return (byte) gfilog[gflog[a] - gflog[b]];
    }

}
