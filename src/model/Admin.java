package model;

import util.PasswordUtil;

/**
 * Admin (Manager) Class — Extends BankEntity
 * Demonstrates: INHERITANCE, POLYMORPHISM
 */
public class Admin extends BankEntity {
    private final String username;
    private String passwordHash;

    public Admin(String id, String name, String username, String password) {
        super(id, name);
        this.username = username;
        this.passwordHash = PasswordUtil.hash(password);
    }

    // POLYMORPHISM: Different implementation of the same method
    @Override
    public String getDetails() {
        return String.format("Manager: %s (@%s) | Role: Branch Manager", getName(), username);
    }

    public boolean verifyPassword(String pwd) {
        return passwordHash.equals(PasswordUtil.hash(pwd));
    }

    public String getUsername() {
        return username;
    }

    public void resetPassword(String newPwd) {
        if (newPwd == null || newPwd.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        this.passwordHash = PasswordUtil.hash(newPwd);
    }
}
