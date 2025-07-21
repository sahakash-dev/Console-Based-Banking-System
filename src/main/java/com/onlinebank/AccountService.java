package com.onlinebank;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AccountService {

    // Create new account and return generated account number
    public int createAccount(Account acc, String username) throws Exception {
        try (Connection con = DBUtil.getConnection()) {
            String sql = "INSERT INTO accounts (name, age, phone, balance, account_type, username, last_interest_applied) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, acc.getName());
            ps.setInt(2, acc.getAge());
            ps.setString(3, acc.getPhone());
            ps.setDouble(4, acc.getBalance());
            ps.setString(5, acc.getAccountType());
            ps.setString(6, username);
            ps.setObject(7, acc.getLastInterestApplied());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // Return generated account number
            } else {
                throw new Exception("Failed to create account.");
            }
        }
    }

    // Retrieve all accounts for a given username
    public List<Account> getAccountsForUser(String username) throws Exception {
        List<Account> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection()) {
            String sql = "SELECT * FROM accounts WHERE username = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToAccount(rs));
            }
        }
        return list;
    }

    // Find a specific account by its account number
    public Account findAccount(int accNo) throws Exception {
        try (Connection con = DBUtil.getConnection()) {
            String sql = "SELECT * FROM accounts WHERE account_number = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, accNo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToAccount(rs);
            }
            return null;
        }
    }

    // Update only the balance of an account
    public void updateAccount(Account acc) throws Exception {
        try (Connection con = DBUtil.getConnection()) {
            String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDouble(1, acc.getBalance());
            ps.setInt(2, acc.getAccountNumber());
            ps.executeUpdate();
        }
    }

    // Update both balance and last_interest_applied
    public void updateAccountWithInterestDate(Account acc) throws Exception {
        try (Connection con = DBUtil.getConnection()) {
            String sql = "UPDATE accounts SET balance = ?, last_interest_applied = ? WHERE account_number = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDouble(1, acc.getBalance());
            ps.setDate(2, acc.getLastInterestApplied() != null ? Date.valueOf(acc.getLastInterestApplied()) : null);
            ps.setInt(3, acc.getAccountNumber());
            ps.executeUpdate();
        }
    }

    // Get the latest (highest) account number
    public int findLatestAccountNumber() throws Exception {
        try (Connection con = DBUtil.getConnection()) {
            String sql = "SELECT MAX(account_number) AS max_acc FROM accounts";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("max_acc");
            } else {
                throw new Exception("No accounts found.");
            }
        }
    }

    
    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        return new Account(
            rs.getInt("account_number"),
            rs.getString("name"),
            rs.getInt("age"),
            rs.getString("phone"),
            rs.getDouble("balance"),
            rs.getString("account_type"),
            rs.getObject("last_interest_applied") != null
                ? ((Date) rs.getObject("last_interest_applied")).toLocalDate()
                : null
        );
    }
}
