//This connects registration and login to your existing users table.
package services;

import database.DBConnection;
import security.PasswordUtil;

import java.sql.*;

public class UserService {

    public boolean register(
            String name, String email, String password) {

        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.length() < 8) {
            return false;
        }

        String sql = """
                INSERT INTO users (name, email, password_hash)
                VALUES (?, ?, ?)
                """;

        String hash = PasswordUtil.hashPassword(password);

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, name.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, hash);

            return ps.executeUpdate() == 1;

        } catch (SQLIntegrityConstraintViolationException e) {
            // For example, the email is already registered.
            return false;
        } catch (SQLException e) {
            System.out.println("Registration error: " + e.getMessage());
            return false;
        }
    }

    public int login(String email, String password) {
        String sql = """
                SELECT user_id, password_hash
                FROM users
                WHERE email = ?
                """;

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, email.trim().toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordUtil.verifyPassword(
                        password, rs.getString("password_hash"))) {
                    return rs.getInt("user_id");
                }
            }

        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }

        return -1;
    }

    
public String getUserName(int userId) {
    String sql = "SELECT name FROM users WHERE user_id = ?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, userId);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("name");
            }
        }

    } catch (SQLException e) {
        System.out.println("Error fetching user name: " + e.getMessage());
    }

    return "User";
}

}

//The login method returns the actual user ID on success and -1 if authentication fails.