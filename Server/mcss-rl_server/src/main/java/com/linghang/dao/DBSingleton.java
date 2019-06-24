package com.linghang.dao;

import com.linghang.util.ConstantUtil;
import com.linghang.util.PropertiesUtil;
import com.mysql.jdbc.Connection;

import java.sql.SQLException;

public enum DBSingleton {

    INSTANCE;
    private DBConnection DBConn;

    DBSingleton(){
        PropertiesUtil propertiesUtil = new PropertiesUtil(ConstantUtil.SERVER_PROPERTY_NAME);
        String driver = propertiesUtil.getValue("db.driver");
        String username = propertiesUtil.getValue("db.username");
        String password = propertiesUtil.getValue("db.password");
        String url = propertiesUtil.getValue("db.url");
        DBConn = new DBConnection(driver, username, password, url);
    }

    public Connection getConn() {
        return DBConn.getConnection();
    }

    public boolean closeConn(){
        Connection conn = DBConn.getConnection();
        if (conn != null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }
}
