package com.linghang.dao;

import com.mysql.jdbc.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private String driver;
    private String username;
    private String password;
    private String url;
    private Connection connection;

    public DBConnection(String driver, String username, String password, String url){
        this.driver = driver;
        this.username = username;
        this.password = password;
        this.url = url;
        this.Connect();
    }

    private void Connect(){
        try {
            Class.forName(driver);
            connection = (Connection) DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            connection = null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            connection = null;
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
