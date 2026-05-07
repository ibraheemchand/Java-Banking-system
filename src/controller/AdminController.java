package controller;

import model.Admin;
import model.User;
import model.Transaction;
import service.BankService;
import java.util.List;
import java.util.Map;

/**
 * AdminController - Handles Admin Operations
 */
public class AdminController {
    private final BankService bankService;

    public AdminController(BankService bankService) {
        this.bankService = bankService;
    }

    public List<User> viewAllUsers() {
        return bankService.getAllUsers();
    }

    public List<Transaction> viewUserTransactions(String username) {
        return bankService.getUserTransactions(username);
    }

    public void resetUserPassword(String adminUsername, String targetUsername, String newPassword) {
        if (targetUsername == null || targetUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Target username cannot be empty");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        bankService.resetUserPassword(adminUsername, targetUsername, newPassword);
    }

    public Map<String, Double> getBalanceDistribution() {
        return bankService.getBalanceDistribution();
    }

    public int getTotalUsers() {
        return bankService.getTotalUsers();
    }

    public double getTotalBalance() {
        return bankService.getTotalBalance();
    }

    public List<String> getSystemLog() {
        return bankService.getSystemLog();
    }

    public String getAdminDetails() {
        return "Admin Dashboard";
    }

    public double getAverageBalance() {
        int totalUsers = getTotalUsers();
        if (totalUsers == 0) return 0;
        return getTotalBalance() / totalUsers;
    }

    public int getActiveTransactions() {
        int count = 0;
        for (User user : bankService.getAllUsers()) {
            count += user.getAccount().getTransactions().size();
        }
        return count;
    }
}
