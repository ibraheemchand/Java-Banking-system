package service;

import model.Admin;
import model.User;
import model.Transaction;
import exception.DuplicateUserException;
import exception.UserNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * BankService - Singleton Service Layer
 * Demonstrates: ENCAPSULATION, ABSTRACTION, Singleton Pattern
 */
public class BankService {
    private static BankService instance;           // Singleton instance
    private final Map<String, User> users;
    private final Map<String, Admin> admins;
    private final List<String> systemLog;

    private BankService() {
        users = new HashMap<>();
        admins = new HashMap<>();
        systemLog = new ArrayList<>();
        initializeDefaultData();
    }

    public static synchronized BankService getInstance() {
        if (instance == null) {
            instance = new BankService();
        }
        return instance;
    }

    // Initialize with demo data
    private void initializeDefaultData() {
        // Demo Users
        registerUser("Alice Johnson", "alice", "pass123", 5000);
        registerUser("Bob Smith", "bob", "pass456", 3000);
        registerUser("Carol White", "carol", "carol789", 7500);

        // Demo Admin
        admins.put("admin", new Admin("ADM-001", "Admin User", "admin", "admin123"));
    }

    public User registerUser(String name, String username, String pwd, double deposit) {
        if (users.containsKey(username)) {
            throw new DuplicateUserException("Username already exists: " + username);
        }
        User user = new User(generateUserId(), name, username, pwd, deposit);
        users.put(username, user);
        logActivity("SYSTEM", "New user registered: @" + username);
        return user;
    }

    public User loginUser(String username, String password) {
        User user = getUser(username);
        if (!user.verifyPassword(password)) {
            throw new IllegalArgumentException("Invalid password");
        }
        logActivity(username, "Login successful");
        return user;
    }

    public Admin loginAdmin(String username, String password) {
        Admin admin = admins.get(username);
        if (admin == null) {
            throw new UserNotFoundException("Admin not found: " + username);
        }
        if (!admin.verifyPassword(password)) {
            throw new IllegalArgumentException("Invalid password");
        }
        logActivity(username, "Admin login successful");
        return admin;
    }

    public User getUser(String username) {
        User user = users.get(username);
        if (user == null) {
            throw new UserNotFoundException("User not found: " + username);
        }
        return user;
    }

    public Admin getAdmin(String username) {
        Admin admin = admins.get(username);
        if (admin == null) {
            throw new UserNotFoundException("Admin not found: " + username);
        }
        return admin;
    }

    public TransferResult transferMoney(String fromUsername, String toUsername, double amount, String note) {
        User sender = getUser(fromUsername);
        User receiver = getUser(toUsername);

        if (fromUsername.equals(toUsername)) {
            throw new IllegalArgumentException("Cannot transfer to yourself");
        }

        // Atomic transfer — both operations must succeed
        sender.getAccount().debit(amount, "Transfer to @" + toUsername + " - " + note, 
                                  receiver.getAccountNumber());
        receiver.getAccount().transferIn(amount, "Transfer from @" + fromUsername + " - " + note, 
                                         sender.getAccountNumber());

        logActivity(fromUsername, "Transferred PKR " + amount + " to @" + toUsername);
        return new TransferResult(amount, sender.getName(), receiver.getName(), note);
    }

    public void resetUserPassword(String adminUsername, String targetUsername, String newPwd) {
        User user = getUser(targetUsername);
        user.resetPassword(newPwd);
        logActivity(adminUsername, "Password reset for @" + targetUsername);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public List<Transaction> getUserTransactions(String username) {
        User user = getUser(username);
        return user.getAccount().getTransactions();
    }

    public Map<String, Double> getBalanceDistribution() {
        Map<String, Double> distribution = new HashMap<>();
        for (User user : users.values()) {
            distribution.put(user.getUsername(), user.getBalance());
        }
        return distribution;
    }

    public int getTotalUsers() {
        return users.size();
    }

    public double getTotalBalance() {
        return users.values().stream()
                .mapToDouble(User::getBalance)
                .sum();
    }

    public List<String> getSystemLog() {
        return new ArrayList<>(systemLog);
    }

    private void logActivity(String user, String activity) {
        String timestamp = java.time.LocalDateTime.now().toString();
        systemLog.add(String.format("[%s] %s: %s", timestamp, user, activity));
    }

    private String generateUserId() {
        return "USER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Inner class for transfer results
    public static class TransferResult {
        public final double amount;
        public final String senderName;
        public final String receiverName;
        public final String note;

        public TransferResult(double amount, String senderName, String receiverName, String note) {
            this.amount = amount;
            this.senderName = senderName;
            this.receiverName = receiverName;
            this.note = note;
        }

        @Override
        public String toString() {
            return String.format("Transfer successful: %s sent PKR %.2f to %s. Note: %s",
                    senderName, amount, receiverName, note);
        }
    }
}
