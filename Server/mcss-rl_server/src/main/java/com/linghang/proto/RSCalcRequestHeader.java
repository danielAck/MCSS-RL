package com.linghang.proto;

import java.io.Serializable;
import java.util.ArrayList;

public class RSCalcRequestHeader implements Serializable {

    private static final long serialVersionUID = 388993274649025442L;

    private String fileName;
    private ArrayList<String> calcHosts;
    private String redundantBlockRecvHost;
    private long startPos;

    public RSCalcRequestHeader(String fileName, ArrayList<String> calcHosts, String redundantBlockRecvHost, long startPos) {
        this.fileName = fileName;
        this.calcHosts = calcHosts;
        this.redundantBlockRecvHost = redundantBlockRecvHost;
        this.startPos = startPos;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ArrayList<String> getCalcHosts() {
        return calcHosts;
    }

    public void setCalcHosts(ArrayList<String> calcHosts) {
        this.calcHosts = calcHosts;
    }

    public String getRedundantBlockRecvHost() {
        return redundantBlockRecvHost;
    }

    public void setRedundantBlockRecvHost(String redundantBlockRecvHost) {
        this.redundantBlockRecvHost = redundantBlockRecvHost;
    }

    public long getStartPos() {
        return startPos;
    }

    public void setStartPos(long startPos) {
        this.startPos = startPos;
    }
}
