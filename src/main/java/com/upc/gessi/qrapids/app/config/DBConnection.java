package com.upc.gessi.qrapids.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Class that encapsulates the database connection information
 */
@Configuration
@PropertySource(value="classpath:application.properties", encoding="UTF-8")
public class DBConnection {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    public String getURL() {
        return url;
    }
    public String getUser() {
        return username;
    }
    public String getPass() { return password; }

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");

        // We use a configuration file for customise the connection to the URL
        return DriverManager.getConnection(url, username, password);

    }

/* Gessi3 server
    public static String getURL() {return "jdbc:postgresql://localhost:5432/riscoss";}
    public static String getUser() {return "riscoss";}
    public static String getPass() {return "5lG6shrxAt";}
*/
}
