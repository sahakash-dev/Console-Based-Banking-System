package com.onlinebank;

import java.sql.Timestamp;

public class Transaction {
    public int id;
    public int accountNumber;
    public Timestamp timestamp;
    public String type;
    public double amount;
    public String note;

    public Transaction(int id, int accNo, Timestamp timestamp, String type, double amount, String note) {
        this.id = id;
        this.accountNumber = accNo;
        this.timestamp = timestamp;
        this.type = type;
        this.amount = amount;
        this.note = note;
    }

    public Transaction(int accNo, String type, double amount, String note) {
        this.accountNumber = accNo;
        this.type = type;
        this.amount = amount;
        this.note = note;
        this.timestamp = new Timestamp(System.currentTimeMillis()); // Set current time
    }

    @Override
    public String toString() {
        return String.format("[%s] %-10s Rs.%.2f | %s", this.timestamp, this.type, this.amount, this.note);
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
