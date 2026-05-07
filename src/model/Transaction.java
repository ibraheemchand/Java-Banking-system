package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Immutable Transaction Record
 * Demonstrates: ENCAPSULATION (all fields private and final)
 */
public class Transaction {
    private final String id;
    private final String type;                      // credit | debit | transfer-out | transfer-in | initial
    private final double amount;
    private final String description;
    private final String fromAccount;              // null if credit/initial
    private final String toAccount;                // null if debit
    private final double balanceAfter;
    private final LocalDateTime timestamp;

    // Constructor-only assignment, no setters
    public Transaction(String type, double amount, String description, String fromAccount,
                       String toAccount, double balanceAfter) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.balanceAfter = balanceAfter;
        this.timestamp = LocalDateTime.now();
    }

    // Read-only getters
    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public boolean isCredit() {
        return type.equals("credit") || type.equals("initial") || type.equals("transfer-in");
    }

    public boolean isDebit() {
        return type.equals("debit") || type.equals("transfer-out");
    }

    public String formatAmount() {
        return String.format("PKR %.2f", amount);
    }

    public String formatDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %s", formatDate(), type, description, formatAmount());
    }
}
