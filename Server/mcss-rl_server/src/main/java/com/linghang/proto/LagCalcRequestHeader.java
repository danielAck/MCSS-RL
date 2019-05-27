package com.linghang.proto;

import java.io.Serializable;

public class LagCalcRequestHeader implements Serializable {

    private static final long serialVersionUID = -8576747843402952855L;

    private String fileName;
    private int[] x;
    private int[] alpha;
    private boolean encode;

    public LagCalcRequestHeader(String fileName, int[] x, int[] alpha, boolean encode) {
        this.fileName = fileName;
        this.x = x;
        this.alpha = alpha;
        this.encode = encode;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int[] getX() {
        return x;
    }

    public int[] getAlpha() {
        return alpha;
    }

    public boolean isEncode() {
        return encode;
    }

    public void setEncode(boolean encode) {
        this.encode = encode;
    }
}
