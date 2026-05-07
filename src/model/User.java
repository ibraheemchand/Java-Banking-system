package model;

import util.PasswordUtil;

/**
 * User (Customer) Class — Extends BankEntity
 * Demonstrates: INHERITANCE, ENCAPSULATION, POLYMORPHISM
 */
public class User extends BankEntity {
    private final String username;
    private String passwordHash;                   // Stored as hash, never plain text
    private final Account account;

    public User(String id, String fullName, String username, String password, double deposit) {
        super(id, fullName);                       // Calls parent constructor
        this.username = username;
        this.passwordHash = PasswordUtil.hash(password);
        this.account = new Account(id, deposit);
    }

    public boolean verifyPassword(String pwd) {
        return passwordHash.equals(PasswordUtil.hash(pwd));
    }

    public void resetPassword(String newPwd) {
        if (newPwd == null || newPwd.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        this.passwordHash = PasswordUtil.hash(newPwd);
    }

    // POLYMORPHISM: Overrides abstract method from BankEntity
    @Override
    public String getDetails() {
        return String.format("Customer: %s (@%s) | Acc: %s | Balance: PKR %.2f",
                getName(), username, account.getAccountNumber(), account.getBalance());
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public Account getAccount() {
        return account;
    }

    public double getBalance() {
        return account.getBalance();
    }

    public String getAccountNumber() {
        return account.getAccountNumber();
    }
}
