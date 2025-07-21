package com.onlinebank;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class Main {
    static Scanner sc = new Scanner(System.in);
    static AuthService authService = new AuthService();
    static AccountService accountService = new AccountService();
    static TransactionService transactionService = new TransactionService();
    static String currentUser = null;

    public static void main(String[] args) throws Exception {
        while (true) {
            System.out.println("\n=== Welcome to Console Bank ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choice: ");
            int ch = sc.nextInt();
            sc.nextLine();

            switch (ch) {
                case 1:
                    register();
                    break;
                case 2:
                    login();
                    break;
                case 3:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice");
                    break;
            }
        }
    }

    static void register() throws Exception {
        System.out.print("Choose username: ");
        String uname = sc.nextLine();
        System.out.print("Choose password: ");
        String pass = sc.nextLine();
        authService.register(uname, pass);
    }

    static void login() throws Exception {
        System.out.print("Username: ");
        String uname = sc.nextLine();
        System.out.print("Password: ");
        String pass = sc.nextLine();

        if (authService.login(uname, pass)) {
            currentUser = uname;
            System.out.println(" Login successful! Welcome, " + uname);
            autoApplyInterest();
            userMenu();
        } else {
            System.out.println("Login failed!");
        }
    }

    static void autoApplyInterest() throws Exception {
        List<Account> list = accountService.getAccountsForUser(currentUser);
        for (Account acc : list) {
            if (!acc.getAccountType().toLowerCase().startsWith("saving")) continue;

            LocalDate lastApplied = acc.getLastInterestApplied();
            LocalDate today = LocalDate.now();
            if (lastApplied == null || lastApplied.getMonthValue() != today.getMonthValue() || lastApplied.getYear() != today.getYear()) {
                double rate = 0.004;
                double interest = acc.getBalance() * rate;
                acc.setBalance(acc.getBalance() + interest);
                acc.setLastInterestApplied(today);
                accountService.updateAccountWithInterestDate(acc);
                transactionService.recordTransaction(new Transaction(acc.getAccountNumber(), "Interest", interest, "Auto monthly interest"));
                System.out.println("Interest of Rs." + interest + " auto-applied to Account #" + acc.getAccountNumber());
            }
        }
    }

    static void userMenu() throws Exception {
        while (true) {
            System.out.println("\n--- User Menu ---");
            System.out.println("1. Create Account");
            System.out.println("2. View My Accounts");
            System.out.println("3. View Statement");
            System.out.println("4. Withdraw Funds");
            System.out.println("5. Deposit Funds");
            System.out.println("6. Logout");
            System.out.println("7. Add Interest");
            System.out.println("8. Transfer Funds");
            System.out.print("Choice: ");
            int ch = sc.nextInt();
            sc.nextLine();
            switch (ch) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    viewAccounts();
                    break;
                case 3:
                    viewStatement();
                    break;
                case 4:
                    withdrawFunds();
                    break;
                case 5:
                    depositFunds();
                    break;
                case 6:
                    currentUser = null;
                    return;
                case 7:
                    addInterest();
                    break;
                case 8:
                    transferFunds();
                    break;
                default:
                    System.out.println("Invalid choice");
                    break;
            }
        }
    }

    static void createAccount() throws Exception {
        sc.nextLine();
        System.out.print("Name: ");
        String name = sc.nextLine();
        System.out.print("Age: ");
        int age = sc.nextInt();
        sc.nextLine();
        System.out.print("Phone: ");
        String phone = sc.nextLine();
        System.out.print("Initial Deposit: ");
        double bal = sc.nextDouble();
        sc.nextLine();
        System.out.print("Account Type (Savings/Current): ");
        String type = sc.nextLine();

        Account acc = new Account(0, name, age, phone, bal, type);
        int accNo = accountService.createAccount(acc, currentUser);
        transactionService.recordTransaction(new Transaction(accNo, "Deposit", bal, "Initial deposit"));
    }

    static void viewAccounts() throws Exception {
        List<Account> list = accountService.getAccountsForUser(currentUser);
        for (Account acc : list) {
            System.out.printf("Account #%d | Name: %s | Balance: Rs.%.2f | Type: %s | Interest Applied: %s\n",
                    acc.getAccountNumber(), acc.getName(), acc.getBalance(), acc.getAccountType(), acc.getLastInterestApplied());
        }
    }

    static void viewStatement() throws Exception {
        System.out.print("Account Number: ");
        int accNo = sc.nextInt();
        System.out.print("Month (1-12): ");
        int month = sc.nextInt();
        System.out.print("Year: ");
        int year = sc.nextInt();

        Account account = accountService.findAccount(accNo);
        double openingBalance = transactionService.getOpeningBalance(accNo, month, year);
        List<Transaction> txns = transactionService.getMonthlyStatement(accNo, month, year);

        if (txns.isEmpty()) {
            System.out.println("No transactions");
        } else {
            txns.forEach(System.out::println);
            double totalInterest = txns.stream()
                .filter(tx -> tx.getType().equalsIgnoreCase("Interest"))
                .mapToDouble(Transaction::getAmount)
                .sum();

            System.out.println("Total Interest Earned: Rs." + totalInterest);
            System.out.println("Current Balance: Rs." + account.getBalance());
            generatePDFStatement(accNo, month, year, txns, totalInterest, openingBalance, account.getBalance());
        }
    }

static void generatePDFStatement(int accNo, int month, int year, List<Transaction> txns,
                                 double totalInterest, double openingBalance, double currentBalance) throws Exception {
    Document doc = new Document();
    String filename = "Statement_" + accNo + "_" + month + "-" + year + ".pdf";
    PdfWriter.getInstance(doc, new FileOutputStream(filename));
    doc.open();

    doc.add(new Paragraph("Bank Statement for Account #: " + accNo));
    doc.add(new Paragraph("Month/Year: " + month + "/" + year));
    doc.add(new Paragraph("\n"));

    PdfPTable table = new PdfPTable(4);
    table.setWidths(new float[]{3, 2, 2, 5});
    table.setWidthPercentage(100);
    table.addCell("Date");
    table.addCell("Type");
    table.addCell("Amount");
    table.addCell("Note");

    for (Transaction tx : txns) {
        table.addCell(tx.timestamp.toString());
        table.addCell(tx.type);
        table.addCell(String.format("Rs.%.2f", tx.amount));
        table.addCell(tx.note);
    }

    doc.add(table);
    doc.add(new Paragraph("\n"));
    doc.add(new Paragraph("Opening Balance: Rs." + String.format("%.2f", openingBalance)));
    doc.add(new Paragraph("Total Interest Earned: Rs." + String.format("%.2f", totalInterest)));
    doc.add(new Paragraph("Closing Balance: Rs." + String.format("%.2f", currentBalance)));

    doc.close();
    System.out.println("PDF generated: " + filename);

    File pdfFile = new File(filename);
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(pdfFile);
    }
}


    static void withdrawFunds() throws Exception {
        System.out.print("Enter account number: ");
        int accNo = sc.nextInt();
        System.out.print("Amount to withdraw: ");
        double amount = sc.nextDouble();

        Account acc = accountService.findAccount(accNo);
        if (acc == null || acc.getBalance() < amount) {
            System.out.println("Insufficient balance or account not found.");
            return;
        }

        acc.setBalance(acc.getBalance() - amount);
        accountService.updateAccount(acc);
        transactionService.recordTransaction(new Transaction(accNo, "Withdrawal", amount, "User withdrawal"));
        System.out.println("Withdrawal successful.");
    }

    static void depositFunds() throws Exception {
        System.out.print("Enter account number: ");
        int accNo = sc.nextInt();
        System.out.print("Amount to deposit: ");
        double amount = sc.nextDouble();

        Account acc = accountService.findAccount(accNo);
        if (acc == null) {
            System.out.println("Account not found.");
            return;
        }

        acc.setBalance(acc.getBalance() + amount);
        accountService.updateAccount(acc);
        transactionService.recordTransaction(new Transaction(accNo, "Deposit", amount, "User deposit"));
        System.out.println("Deposit successful.");
    }

    static void addInterest() throws Exception {
        System.out.print("Enter account number: ");
        int accNo = sc.nextInt();

        Account acc = accountService.findAccount(accNo);
        if (acc == null || !acc.getAccountType().toLowerCase().startsWith("saving")) {
            System.out.println("Account not found or not eligible for interest.");
            return;
        }

        double interestRate = 0.004;
        double interest = acc.getBalance() * interestRate;
        acc.setBalance(acc.getBalance() + interest);
        acc.setLastInterestApplied(LocalDate.now());
        accountService.updateAccountWithInterestDate(acc);
        transactionService.recordTransaction(new Transaction(accNo, "Interest", interest, "Monthly interest credited"));
        System.out.println("Interest of Rs." + interest + " added to Account #" + accNo);
    }

    static void transferFunds() throws Exception {
        System.out.print("Enter your account number: ");
        int fromAcc = sc.nextInt();
        System.out.print("Enter recipient's account number: ");
        int toAcc = sc.nextInt();
        System.out.print("Amount to transfer: ");
        double amount = sc.nextDouble();

        if (fromAcc == toAcc) {
            System.out.println("Cannot transfer to the same account.");
            return;
        }

        Account sender = accountService.findAccount(fromAcc);
        Account recipient = accountService.findAccount(toAcc);

        if (sender == null || recipient == null) {
            System.out.println("One or both accounts not found.");
            return;
        }

        if (sender.getBalance() < amount) {
            System.out.println("Insufficient balance.");
            return;
        }

        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount);

        accountService.updateAccount(sender);
        accountService.updateAccount(recipient);

        transactionService.recordTransaction(new Transaction(fromAcc, "Transfer Out", amount, "Transfer to Account #" + toAcc));
        transactionService.recordTransaction(new Transaction(toAcc, "Transfer In", amount, "Received from Account #" + fromAcc));

        System.out.println("Rs." + amount + " transferred from Account #" + fromAcc + " to Account #" + toAcc);
    }
}
