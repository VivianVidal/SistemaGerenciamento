
package com.barraca.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    public static Connection getConnection() throws SQLException {
        String url = AppConfig.get("db.url", "");
        String user = AppConfig.get("db.user", "");
        String pass = AppConfig.get("db.password", "");
        return DriverManager.getConnection(url, user, pass);
    }
}
