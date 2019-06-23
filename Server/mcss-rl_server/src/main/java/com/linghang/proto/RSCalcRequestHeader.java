package com.linghang.proto;

import java.io.Serializable;
import java.util.ArrayList;

public class RSCalcRequestHeader implements Serializable {

    private static final long serialVersionUID = 388993274649025442L;

    private String calcFileName;
    private String calcFilePath;
    private String redundancySaveFileName;
    private String redundancySaveFilePath;
    private ArrayList<String> calcHosts;
    private String redundantBlockRecvHost;
    private int blockIdx;
    private boolean isDownLoad;

    public RSCalcRequestHeader(String calcFileName, String calcFilePath, String redundancySaveFileName, String redundancySaveFilePath,
                               ArrayList<String> calcHosts, String redundantBlockRecvHost, int blockIdx, boolean isDownLoad) {
        this.calcFileName = calcFileName;
        this.calcFilePath = calcFilePath;
        this.redundancySaveFileName = redundancySaveFileName;
        this.redundancySaveFilePath = redundancySaveFilePath;
        this.calcHosts = calcHosts;
        this.redundantBlockRecvHost = redundantBlockRecvHost;
        this.blockIdx = blockIdx;
        this.isDownLoad = isDownLoad;
    }

    public String getCalcFileName() {
        return calcFileName;
    }

    public void setCalcFileName(String calcFileName) {
        this.calcFileName = calcFileName;
    }

    public String getCalcFilePath() {
        return calcFilePath;
    }

    public void setCalcFilePath(String calcFilePath) {
        this.calcFilePath = calcFilePath;
    }

    public String getRedundancySaveFileName() {
        return redundancySaveFileName;
    }

    public void setRedundancySaveFileName(String redundancySaveFileName) {
        this.redundancySaveFileName = redundancySaveFileName;
    }

    public String getRedundancySaveFilePath() {
        return redundancySaveFilePath;
    }

    public void setRedundancySaveFilePath(String redundancySaveFilePath) {
        this.redundancySaveFilePath = redundancySaveFilePath;
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

    public int getBlockIdx() {
        return blockIdx;
    }

    public void setBlockIdx(int blockIdx) {
        this.blockIdx = blockIdx;
    }

    public boolean isDownLoad() {
        return isDownLoad;
    }

    public void setDownLoad(boolean downLoad) {
        isDownLoad = downLoad;
    }
}
