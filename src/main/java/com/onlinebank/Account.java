package com.onlinebank;

import java.time.LocalDate;

public class Account {
    private int accountNumber;
    private String name;
    private int age;
    private String phone;
    private double balance;
    private String accountType;
    private LocalDate lastInterestApplied;

    // ✅ Constructor WITH lastInterestApplied for DB mapping
    public Account(int accountNumber, String name, int age, String phone, double balance, String accountType, LocalDate lastInterestApplied) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.age = age;
        this.phone = phone;
        this.balance = balance;
        this.accountType = accountType;
        this.lastInterestApplied = lastInterestApplied;
    }

    // ✅ Optional Constructor for UI-created accounts (e.g., during creation)
    public Account(int accountNumber, String name, int age, String phone, double balance, String accountType) {
        this(accountNumber, name, age, phone, balance, accountType, null);
    }

    // 🔁 Getters
    public int getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getPhone() { return phone; }
    public double getBalance() { return balance; }
    public String getAccountType() { return accountType; }
    public LocalDate getLastInterestApplied() { return lastInterestApplied; }

    // 🔁 Setters
    public void setBalance(double balance) { this.balance = balance; }
    public void setLastInterestApplied(LocalDate lastInterestApplied) {
        this.lastInterestApplied = lastInterestApplied;
    }

    @Override
    public String toString() {
        return "Account #" + accountNumber +
               " | Name: " + name +
               " | Balance: Rs." + String.format("%.2f", balance) +
               " | Type: " + accountType +
               (lastInterestApplied != null
                   ? " | Interest Applied: " + lastInterestApplied
                   : "");
    }
}
