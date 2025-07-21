package com.onlinebank;

import java.sql.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class AuthService {

    // Register a new user
    public void register(String username, String password) throws Exception {
        if (userExists(username)) {
            System.out.println("Username already exists!");
            return;
        }

        String hash = hashPassword(password);
        try (Connection con = DBUtil.getConnection()) {
            String sql = "INSERT INTO users (username, hashed_password) VALUES (?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.executeUpdate();
            System.out.println("User registered successfully!");
        }
    }

    // Authenticate user credentials
    public boolean login(String username, String password) throws Exception {
        String hash = hashPassword(password);
        try (Connection con = DBUtil.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND hashed_password = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, hash);
            ResultSet rs = ps.executeQuery();
            return rs.next(); 
        }
    }

    // Check if user already exists
    private boolean userExists(String username) throws Exception {
        try (Connection con = DBUtil.getConnection()) {
            String sql = "SELECT username FROM users WHERE username = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    // Secure SHA-256 password hashing
    private String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
