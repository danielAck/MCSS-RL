package com.linghang.util;

import java.util.HashMap;

public class CalcUitl {

    private static int[] gfilog;
    private static int[] gflog;
    private static final int CALC_W = 8;

    public CalcUitl() {
        geneGfilogDict();
        geneGflogDict(gfilog);
    }

    private void geneGfilogDict(){

        HashMap<Integer, Integer> primitivePolyDict = new HashMap<Integer, Integer>();
        primitivePolyDict.put(4, 0b10011);
        primitivePolyDict.put(8, (1<<8) + 0b11101);
        int gfTotalNum = 1 << CALC_W;

        Integer primitivePoly = primitivePolyDict.get(CALC_W);
        gfilog = new int[gfTotalNum];
        gfilog[0] = 1;
        for (int i = 1; i < gfTotalNum - 1; i++){
            int temp = gfilog[i-1] << 1;
            if ((temp & gfTotalNum) != 0){
                temp = temp ^ primitivePoly;
            }
            gfilog[i] = temp;
        }
    }

    private void geneGflogDict(int[] gfilog){
        gflog = new int[gfilog.length];
        for (int i = 0; i < gfilog.length - 1; i++){
            gflog[gfilog[i]] = i;
        }
    }

    public int[] getGfilog() {
        return gfilog;
    }

    public int[] getGflog() {
        return gflog;
    }
}
