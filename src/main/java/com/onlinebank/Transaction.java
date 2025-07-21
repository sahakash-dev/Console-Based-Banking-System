package com.onlinebank;

import java.sql.Timestamp;

public class Transaction {
    public int id;
    public int accountNumber;
    public Timestamp timestamp;
    public String type;
    public double amount;
    public String note;

    // Constructor for DB-fetched transaction (with ID and timestamp)
    public Transaction(int id, int accNo, Timestamp timestamp, String type, double amount, String note) {
        this.id = id;
        this.accountNumber = accNo;
        this.timestamp = timestamp;
        this.type = type;
        this.amount = amount;
        this.note = note;
    }

    // Constructor for new transaction (auto timestamp)
    public Transaction(int accNo, String type, double amount, String note) {
        this.accountNumber = accNo;
        this.type = type;
        this.amount = amount;
        this.note = note;
        this.timestamp = new Timestamp(System.currentTimeMillis()); // auto timestamp
    }

    // ✅ Getters
    public int getId() {
        return id;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getNote() {
        return note;
    }


    @Override
    public String toString() {
        return String.format("[%s] %-10s Rs.%.2f | %s", timestamp.toString(), type, amount, note);
    }
}
