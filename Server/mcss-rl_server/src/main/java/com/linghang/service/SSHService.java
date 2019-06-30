package com.linghang.service;

import com.linghang.pojo.CloudFile;
import com.linghang.ssh.RemoteConnect;
import com.linghang.ssh.SSHConnection;
import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.linghang.util.Util;

import java.util.ArrayList;

public class SSHService {

    public SSHService() {
    }

    public boolean DeleteRemoteFile(String host, String fileName){
        String cmd = "rm -rf /linghang/dsz/" + Util.genePartName(fileName);
        RemoteConnect remoteConnect = new RemoteConnect(host, "root", "123456");
        boolean loginSuccess = SSHConnection.login(remoteConnect);
        if (loginSuccess){
            ArrayList<String> res = SSHConnection.execute(cmd);
            if (res != null){
                if (res.size() == 0)
                    return true;
            }
        }
        SSHConnection.closeConnection();
        return false;
    }

    public ArrayList<CloudFile> getPartFileList(String host){
        String cmd = "ls /linghang/dsz/part";
        RemoteConnect remoteConnect = new RemoteConnect(host, "root", "123456");
        ArrayList<CloudFile> res = new ArrayList<>();
        boolean loginSuccess = SSHConnection.login(remoteConnect);
        if (loginSuccess){
            ArrayList<String> temp = SSHConnection.execute(cmd);
            if (temp != null){
                for (String fileName : temp)
                    res.add(new CloudFile(fileName, host));
                return res;
            }
        }
        SSHConnection.closeConnection();
        return null;
    }

    public ArrayList<CloudFile> getAllPartFileList(){
        PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
        ArrayList<CloudFile> res = new ArrayList<>();
        String[] hosts = new String[4];
        for (int i = 0; i < 4; i++){
            hosts[i] = propertiesUtil.getValue("host.slave" + (i+1));
        }
        for (String host : hosts){
            ArrayList<CloudFile> temp = getPartFileList(host);
            if (temp != null)
                res.addAll(temp);
            else{
                System.err.println("======== ERROR OCCURS WHILE GETTING FILE LIST FROM " + host + " ========");
                return null;
            }
        }
        return res;
    }
}
