package com.linghang.proto;

import java.io.Serializable;
import java.util.ArrayList;

public class RSCalcRequestHeader implements Serializable {

    private static final long serialVersionUID = 388993274649025442L;

    private String remoteFileName;
    private String remoteFilePath;
    private ArrayList<String> calcHosts;
    private String redundantBlockRecvHost;
    private long startPos;

    public RSCalcRequestHeader(String remoteFileName, String remoteFilePath, ArrayList<String> calcHosts, String redundantBlockRecvHost, long startPos) {
        this.remoteFileName = remoteFileName;
        this.remoteFilePath = remoteFilePath;
        this.calcHosts = calcHosts;
        this.redundantBlockRecvHost = redundantBlockRecvHost;
        this.startPos = startPos;
    }

    public String getRemoteFileName() {
        return remoteFileName;
    }

    public void setRemoteFileName(String remoteFileName) {
        this.remoteFileName = remoteFileName;
    }

    public String getRemoteFilePath() {
        return remoteFilePath;
    }

    public void setRemoteFilePath(String remoteFilePath) {
        this.remoteFilePath = remoteFilePath;
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
