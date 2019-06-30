package com.linghang.ssh;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import java.io.*;
import java.util.ArrayList;

public class SSHConnection {

    private static final String DEFAULT_CHARSET = "UTF-8";
    private static Connection conn = null;

    public static Boolean login(RemoteConnect remoteConnect) {
        boolean flag = false;
        try {
            conn = new Connection(remoteConnect.getIp());
            conn.connect();// 连接
            flag = conn.authenticateWithPassword(remoteConnect.getUsername(), remoteConnect.getPassword());// 认证
            if (!flag){
                System.out.println("======== CONNECT TO " + remoteConnect.getIp() + " IN SSH FAILED ========");
                conn.close();
                conn = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static ArrayList<String> execute(String cmd){
        ArrayList<String> result = null;
        try {
            Session session = conn.openSession();// 打开一个会话
            session.execCommand(cmd);// 执行命令
            result = processStdout(session.getStdout());
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static ArrayList<String> processStdout(InputStream in){
        ArrayList<String> result = new ArrayList<>();
        InputStream stdout = new StreamGobbler(in);
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(stdout, DEFAULT_CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        try{
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
            return result;
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static void closeConnection(){
        if (conn != null){
            conn.close();
            conn = null;
        }
    }

}
