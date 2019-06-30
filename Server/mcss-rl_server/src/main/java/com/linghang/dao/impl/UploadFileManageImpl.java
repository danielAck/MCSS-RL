package com.linghang.dao.impl;

import com.linghang.dao.DBConnection;
import com.linghang.dao.DBSingleton;
import com.linghang.dao.UploadFileManageable;
import com.linghang.pojo.CloudFile;
import com.linghang.pojo.UploadDetail;
import com.linghang.pojo.UploadFile;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UploadFileManageImpl implements UploadFileManageable {

    private Connection conn;
    private PreparedStatement pstmt;

    public UploadFileManageImpl(){
        conn = DBSingleton.INSTANCE.getConn();
    }

    @Override
    public Integer insertUploadFile(UploadFile file) {
        String sql = "insert into upload_file (filename,subfix,length) values (?,?,?)";
        if (conn == null)
            return -1;

        try {
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
            pstmt.setString(1, file.getFileName());
            pstmt.setString(2,file.getSubfix());
            pstmt.setLong(3, file.getLength());
            int i = pstmt.executeUpdate();
            pstmt.close();

            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public Integer insertUploadDetail(UploadDetail detail) {
        String sql = "insert into upload_detail (filename,cloud_id,ip) values (?,?,?)";
        if (conn == null)
            return -1;

        try {
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
            pstmt.setString(1, detail.getFileName());
            pstmt.setInt(2,detail.getCloudId());
            pstmt.setString(3, detail.getIp());
            int i = pstmt.executeUpdate();
            pstmt.close();

            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public ArrayList<CloudFile> getAllCloudRecords() {
        String sql = "select upload_file.filename,subfix,ip from upload_file,upload_detail where upload_file.filename=upload_detail.filename and upload_file.status = 2";
        try {
            ArrayList<CloudFile> uploadFiles = new ArrayList<CloudFile>();
            pstmt = (PreparedStatement)conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                uploadFiles.add(new CloudFile(rs.getString(1) + "\\." + rs.getString(2), rs.getString(3)));
            }
            return uploadFiles;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ArrayList<CloudFile> getUploadedRecords() {
        String sql = "select * from upload_file where status = 2";
        try {
            ArrayList<CloudFile> uploadFiles = new ArrayList<CloudFile>();
            pstmt = (PreparedStatement)conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                uploadFiles.add(new CloudFile(rs.getString(1) + "\\." + rs.getString(2), rs.getString(3)));
            }
            return uploadFiles;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UploadFile getUploadFileByFileName(String fileName) {
        String sql = "select * from upload_file where filename = ?";
        try {
            pstmt = (PreparedStatement)conn.prepareStatement(sql);
            pstmt.setString(1, fileName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                return new UploadFile(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getLong(4), rs.getInt(5));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    public ArrayList<UploadDetail> getUploadDetailByFileName(String fileName) {
        String sql = "select * from upload_detail where filename = ?";
        ArrayList<UploadDetail> uploadedDetails = new ArrayList<>();
        try {
            pstmt = (PreparedStatement)conn.prepareStatement(sql);
            pstmt.setString(1, fileName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                uploadedDetails.add(new UploadDetail(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4)));
            }
            return uploadedDetails;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UploadDetail getUploadDetailByFileNameAndCloudId(String fileName, Integer cloudId) {
        String sql = "select * from upload_detail where filename = ? and cloud_id = ?";
        try {
            pstmt = (PreparedStatement)conn.prepareStatement(sql);
            pstmt.setString(1, fileName);
            pstmt.setInt(2, cloudId);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return new UploadDetail(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4));
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Integer getCloudIdByFileNameAndHost(String fileName, String host) {
        String sql = "select cloud_id from upload_detail where filename = ? and ip = ?";
        try {
            pstmt = (PreparedStatement)conn.prepareStatement(sql);
            pstmt.setString(1, fileName);
            pstmt.setString(2, host);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public String getRedundantHostByFileName(String fileName) {
        String sql = "select ip from upload_detail where filename = ? and cloud_id = 3";
        try {
            pstmt = (PreparedStatement)conn.prepareStatement(sql);
            pstmt.setString(1, fileName);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int[] getXValues() {
        String sql = "select * from x_value";
        int[] res = new int[3];
        try {
            pstmt = (PreparedStatement)conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            for (int i = 0; i < 3; i++){
                res[i] = rs.getInt(i+1);
            }
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Integer checkFileUploadedByStatus(String fileName, Integer status) {
        String sql = "select COUNT(*) from upload_file where filename = ? and status = ?";
        try {
            pstmt = (PreparedStatement)conn.prepareStatement(sql);
            pstmt.setString(1, fileName);
            pstmt.setInt(2, status);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public Integer checkXValueSet() {
        String sql = "select COUNT(*) from x_value;";
        try {
            pstmt = (PreparedStatement)conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public Integer deleteUploadFileByFileName(String fileName) {
        String sql = "DELETE FROM upload_file WHERE filename=?";
        try {
            pstmt = (PreparedStatement)conn.prepareStatement(sql);
            pstmt.setString(1, fileName);
            int i = pstmt.executeUpdate();
            pstmt.close();

            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public Integer deleteUploadDetailByFileName(String fileName) {
        String sql = "DELETE FROM upload_detail WHERE filename=?";
        try {
            pstmt = (PreparedStatement)conn.prepareStatement(sql);
            pstmt.setString(1, fileName);
            int i = pstmt.executeUpdate();
            pstmt.close();

            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public Integer deleteUploadDetailByFileNameAndCloudId(String fileName, Integer cloudId) {
        String sql = "DELETE FROM upload_detail WHERE filename=? AND cloud_id=?";
        try {
            pstmt = (PreparedStatement)conn.prepareStatement(sql);
            pstmt.setString(1, fileName);
            pstmt.setInt(2, cloudId);
            int i = pstmt.executeUpdate();
            pstmt.close();

            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void closeConn(){
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        UploadFileManageable uploadFileService = new UploadFileManageImpl();
        int[] res = uploadFileService.getXValues();
        if (res != null)
            for (int i : res)
                System.out.print(i + ", ");
    }
}
