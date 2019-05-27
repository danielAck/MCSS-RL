package com.linghang;

import com.linghang.rpc.NameNode;

public class DownLoadTest {

    public static void main(String[] args) {
        NameNode nameNode = new NameNode(true);
        String fileName = "1M.pdf";
        int[] selectedIdx = new int[]{0, 1, 2};
        nameNode.download(fileName, selectedIdx);
    }

}
