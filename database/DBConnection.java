
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DBConnection {

    private static Properties properties = new Properties();

    static {
        try (FileInputStream file =
                 new FileInputStream("config.properties")) {

            properties.load(file);

        } catch (IOException e) {
            throw new RuntimeException(
                "Unable to load database configuration", e);
        }
    }

    public static Connection getConnection()
            throws SQLException {

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        return DriverManager.getConnection(url, user, password);
    }
}

    

