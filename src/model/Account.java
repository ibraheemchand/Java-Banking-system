package model;

import exception.InsufficientFundsException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Account Manager - Balance Manager
 * Demonstrates: ENCAPSULATION (balance strictly controlled)
 */
public class Account {
    private final String accountNumber;
    private double balance;                         // Private — only modified via credit/debit
    private final List<Transaction> transactions;
    private final String ownerId;

    public Account(String ownerId, double initialDeposit) {
        this.ownerId = ownerId;
        this.accountNumber = generateAccountNumber();
        this.balance = 0;
        this.transactions = new ArrayList<>();
        
        if (initialDeposit > 0) {
            credit(initialDeposit, "Opening Deposit");
        }
    }

    public boolean hasSufficientFunds(double amount) {
        return balance >= amount;
    }

    public Transaction credit(double amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        balance += amount;
        Transaction t = new Transaction("credit", amount, description, null, accountNumber, balance);
        transactions.add(t);
        return t;
    }

    public Transaction debit(double amount, String description, String toAccount) {
        if (!hasSufficientFunds(amount)) {
            throw new InsufficientFundsException("Insufficient balance. Required: PKR " + amount + 
                ", Available: PKR " + balance);
        }
        balance -= amount;
        Transaction t = new Transaction("transfer-out", amount, description, accountNumber, toAccount, balance);
        transactions.add(t);
        return t;
    }

    public Transaction transferIn(double amount, String description, String fromAccount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        balance += amount;
        Transaction t = new Transaction("transfer-in", amount, description, fromAccount, accountNumber, balance);
        transactions.add(t);
        return t;
    }

    // Getters (no setters for balance — enforces data integrity)
    public double getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    private String generateAccountNumber() {
        return "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
