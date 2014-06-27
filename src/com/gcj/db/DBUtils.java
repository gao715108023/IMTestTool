package com.gcj.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBUtils {

    private Connection conn;
    private PreparedStatement pst;
    String databaseName = "test";
    String host = "10.15.144.76";
    String port = "3306";
    String username = "test";
    String password = "111111";

    public DBUtils() {
        super();
    }

    public DBUtils(String databaseName, String host, String port, String username, String password) {
        super();
        this.databaseName = databaseName;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public void getConn() {
        String driverName = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://" + host + ":" + port + "/" + databaseName;
        try {
            Class.forName(driverName);
            conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(false);
            System.out.println("连接成功！");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addBatch(String obj) {
        try {
            pst.setString(1, obj);
            pst.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void commitBatch() {
        try {
            pst.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (pst != null)
                pst.close();
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        DBUtils dbUtils = new DBUtils();
        try {
            dbUtils.getConn();
        } finally {
            dbUtils.close();
        }
    }
}
