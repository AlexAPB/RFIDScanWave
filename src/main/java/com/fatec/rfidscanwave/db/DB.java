package com.fatec.rfidscanwave.db;

import com.fatec.rfidscanwave.ScanWave;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DB {
    private static Connection c = null;

    public static Connection getConnection(){
        if(c == null){
            try {
                Properties properties = loadProperties();
                String url = properties.getProperty("dburl");
                c = DriverManager.getConnection(url, properties);
            } catch(RuntimeException | SQLException e){
                e.printStackTrace();
            }
        }

        return c;
    }

    private static Properties loadProperties(){
        try(FileInputStream fs = new FileInputStream(ScanWave.class.getResource("/db.properties").getFile())){
            Properties properties = new Properties();
            properties.load(fs);
            return properties;
        } catch(IOException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
