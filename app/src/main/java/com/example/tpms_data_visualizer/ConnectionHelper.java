package com.example.tpms_data_visualizer;

import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionHelper {
    Connection con;
    static String dbHost = "sql8.freesqldatabase.com" ;
    static String dbPortNumber = "3306";
    static String dbPassword = "JZVtahyQ8F";
    static String dbUser = "sql8655171";
    static String dbName = "sql8655171";

    public Connection getConnection(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String connectionURL = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");

            // The format is: "jdbc:mysql://hostname:port/databaseName", "username", "password"
            connectionURL = "jdbc:mysql://" + dbHost + ":" + dbPortNumber + "/" + dbName;
            connection = DriverManager.getConnection(connectionURL, dbUser, dbPassword);

        }
        catch (Exception ex){
            Log.e("PROBLEM", ex.getMessage());
        }

        return connection;
    }

}
