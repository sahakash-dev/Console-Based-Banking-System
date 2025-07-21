package com.onlinebank;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {

    // Records a new transaction
    public void recordTransaction(Transaction txn) throws Exception {
        try (Connection con = DBUtil.getConnection()) {
            String sql = "INSERT INTO transactions (account_number, type, amount, note) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, txn.getAccountNumber());
            ps.setString(2, txn.getType());
            ps.setDouble(3, txn.getAmount());
            ps.setString(4, txn.getNote());
            ps.executeUpdate();
        }
    }

    // Fetches all transactions for a given account, month, and year
    public List<Transaction> getMonthlyStatement(int accNo, int month, int year) throws Exception {
        List<Transaction> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection()) {
            String sql = "SELECT * FROM transactions " +
                         "WHERE account_number = ? AND MONTH(timestamp) = ? AND YEAR(timestamp) = ? " +
                         "ORDER BY timestamp";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, accNo);
            ps.setInt(2, month);
            ps.setInt(3, year);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Transaction(
                    rs.getInt("id"),
                    rs.getInt("account_number"),
                    rs.getTimestamp("timestamp"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getString("note")
                ));
            }
        }
        return list;
    }

    // Calculates opening balance before the given month/year
    public double getOpeningBalance(int accNo, int month, int year) throws Exception {
        try (Connection con = DBUtil.getConnection()) {
            String sql = "SELECT SUM(" +
                         "  CASE " +
                         "    WHEN type = 'Deposit' THEN amount " +
                         "    WHEN type = 'Interest' THEN amount " +
                         "    WHEN type = 'Withdrawal' THEN -amount " +
                         "    ELSE 0 " +
                         "  END) AS balance " +
                         "FROM transactions " +
                         "WHERE account_number = ? AND timestamp < ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, accNo);
            LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
            ps.setTimestamp(2, Timestamp.valueOf(firstDayOfMonth.atStartOfDay()));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        }
        return 0.0;
    }
}
