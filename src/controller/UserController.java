package controller;

import model.User;
import model.Transaction;
import service.BankService;
import java.util.List;

/**
 * UserController - Handles User Operations (Transfer, View Balance, etc.)
 */
public class UserController {
    private final BankService bankService;

    public UserController(BankService bankService) {
        this.bankService = bankService;
    }

    public BankService.TransferResult transferMoney(String fromUsername, String toUsername, 
                                                      double amount, String note) {
        if (toUsername == null || toUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient username cannot be empty");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (note == null || note.trim().isEmpty()) {
            note = "No description provided";
        }

        return bankService.transferMoney(fromUsername, toUsername, amount, note);
    }

    public double getBalance(String username) {
        User user = bankService.getUser(username);
        return user.getBalance();
    }

    public String getAccountNumber(String username) {
        User user = bankService.getUser(username);
        return user.getAccountNumber();
    }

    public List<Transaction> getTransactionHistory(String username) {
        return bankService.getUserTransactions(username);
    }

    public List<Transaction> getRecentTransactions(String username, int limit) {
        List<Transaction> all = getTransactionHistory(username);
        return all.subList(Math.max(0, all.size() - limit), all.size());
    }

    public String getUserDetails(String username) {
        User user = bankService.getUser(username);
        return user.getDetails();
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = bankService.getUser(username);
        if (!user.verifyPassword(oldPassword)) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }
        user.resetPassword(newPassword);
    }
}
