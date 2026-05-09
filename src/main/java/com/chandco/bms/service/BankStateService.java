package com.chandco.bms.service;

import com.chandco.bms.websocket.StateWebSocketHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BankStateService {
    private final ObjectMapper objectMapper;
    private final StateWebSocketHandler webSocketHandler;
    private final Map<String, UserState> users = new LinkedHashMap<>();
    private final Map<String, String> admins = new LinkedHashMap<>();
    private final List<Map<String, Object>> systemLog = new ArrayList<>();

    public BankStateService(ObjectMapper objectMapper, StateWebSocketHandler webSocketHandler) {
        this.objectMapper = objectMapper;
        this.webSocketHandler = webSocketHandler;
        seedDefaults();
    }

    public synchronized Map<String, Object> snapshot() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("users", users.values().stream().map(UserState::toMap).toList());
        snapshot.put("systemLog", new ArrayList<>(systemLog));
        snapshot.put("transactions", allTransactions());
        snapshot.put("stats", stats());
        return snapshot;
    }

    public synchronized List<Map<String, Object>> allTransactions() {
        List<Map<String, Object>> transactions = new ArrayList<>();
        for (UserState user : users.values()) {
            for (TransactionState transaction : user.account.transactions) {
                Map<String, Object> entry = transaction.toMap();
                entry.put("user", user.username);
                transactions.add(entry);
            }
        }
        transactions.sort(Comparator.comparing((Map<String, Object> entry) -> LocalDateTime.parse((String) entry.get("timestamp"))).reversed());
        return transactions;
    }

    public synchronized Map<String, Object> transfer(String fromUsername, String toUsername, double amount, String note) {
        UserState sender = getUserOrThrow(fromUsername);
        UserState receiver = getUserOrThrow(toUsername);

        if (fromUsername.equalsIgnoreCase(toUsername)) {
            throw new IllegalArgumentException("You cannot transfer to yourself.");
        }
        if (Double.isNaN(amount) || amount <= 0) {
            throw new IllegalArgumentException("Invalid transfer amount.");
        }
        if (!sender.account.hasSufficientFunds(amount)) {
            throw new IllegalArgumentException("Insufficient balance.");
        }

        String safeNote = note == null ? "" : note.trim();
        String outDescription = safeNote.isEmpty()
                ? "Transfer to @" + toUsername
                : "Transfer to @" + toUsername + " — \"" + safeNote + "\"";
        String inDescription = safeNote.isEmpty()
                ? "Transfer from @" + fromUsername
                : "Transfer from @" + fromUsername + " — \"" + safeNote + "\"";

        sender.account.debit(amount, outDescription, receiver.account.accountNumber);
        receiver.account.credit(amount, inDescription, sender.account.accountNumber);
        logActivity(fromUsername, "Transferred PKR " + String.format(Locale.US, "%,.2f", amount) + " to @" + toUsername, "blue");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("amount", amount);
        result.put("sender", sender.name);
        result.put("receiver", receiver.name);
        result.put("senderBalance", sender.account.balance);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("result", result);
        payload.put("snapshot", snapshot());
        broadcast("transfer", payload);
        return payload;
    }

    public synchronized Map<String, Object> register(String fullName, String username, String password, double initialDeposit) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (username == null || username.isBlank() || username.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters.");
        }
        if (users.containsKey(username.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Username already taken.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        if (initialDeposit < 100) {
            throw new IllegalArgumentException("Initial deposit must be at least PKR 100.");
        }

        UserState user = new UserState(
                "usr-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT),
                fullName.trim(),
                username.toLowerCase(Locale.ROOT),
                hashPassword(password),
                nowIso(),
                new AccountState("ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT), 0, null)
        );
        user.account.ownerId = user.id;
        user.account.credit(initialDeposit, "Account Opening Deposit", null);
        users.put(user.username, user);
        logActivity("system", "New customer registered: " + fullName.trim(), "green");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", user.toMap());
        payload.put("snapshot", snapshot());
        broadcast("state", payload);
        return payload;
    }

    public synchronized Map<String, Object> resetPassword(String adminUsername, String targetUsername, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        UserState user = getUserOrThrow(targetUsername);
        user.passwordHash = hashPassword(newPassword);
        logActivity(adminUsername, "Password reset for @" + targetUsername, "gold");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("snapshot", snapshot());
        broadcast("state", payload);
        return payload;
    }

    public synchronized Map<String, Object> stats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", users.size());
        stats.put("totalDeposits", users.values().stream().mapToDouble(user -> user.account.balance).sum());
        stats.put("totalTransactions", allTransactions().size());
        String today = LocalDate.now().toString();
        long activeToday = allTransactions().stream()
                .map(entry -> (String) entry.get("timestamp"))
                .filter(timestamp -> timestamp.startsWith(today))
                .map(entry -> (String) entry.get("user"))
                .distinct()
                .count();
        stats.put("activeToday", activeToday);
        return stats;
    }

    public synchronized UserState getUser(String username) {
        return getUserOrThrow(username);
    }

    private void seedDefaults() {
        admins.put("admin", hashPassword("admin123"));

        UserState alice = createUser("usr-001", "Alice Rahman", "alice", "pass123", 50000);
        alice.account.credit(15000, "Salary Deposit - April", null);
        alice.account.credit(5000, "Freelance Payment", null);

        UserState bob = createUser("usr-002", "Bob Hassan", "bob", "pass456", 30000);
        bob.account.credit(10000, "Salary Deposit - April", null);

        UserState carol = createUser("usr-003", "Carol Malik", "carol", "carol789", 75000);
        carol.account.credit(20000, "Business Revenue", null);

        users.put(alice.username, alice);
        users.put(bob.username, bob);
        users.put(carol.username, carol);

        transfer("alice", "bob", 3000, "");
        systemLog.clear();
        logActivity("system", "System initialized with demo accounts", "gold");
    }

    private UserState createUser(String id, String name, String username, String password, double initialDeposit) {
        String accountNumber = "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        UserState user = new UserState(id, name, username, hashPassword(password), nowIso(), new AccountState(accountNumber, 0, id));
        user.account.credit(initialDeposit, "Account Opening Deposit", null);
        return user;
    }

    private UserState getUserOrThrow(String username) {
        UserState user = users.get(username == null ? null : username.toLowerCase(Locale.ROOT));
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        return user;
    }

    private void logActivity(String actor, String message, String color) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("actor", actor);
        entry.put("message", message);
        entry.put("color", color);
        entry.put("ts", nowIso());
        systemLog.add(0, entry);
        if (systemLog.size() > 100) {
            systemLog.remove(systemLog.size() - 1);
        }
    }

    private void broadcast(String type, Map<String, Object> payload) {
        try {
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("type", type);
            message.putAll(payload);
            webSocketHandler.broadcast(objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException ignored) {
        }
    }

    private static String nowIso() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private static String hashPassword(String password) {
        int hash = 0;
        for (char character : password.toCharArray()) {
            hash = ((hash << 5) - hash) + character;
        }
        long normalized = Math.abs((long) hash);
        return "H" + Long.toString(normalized, 36) + Integer.toString(password.length(), 36);
    }

    public static class UserState {
        public String id;
        public String name;
        public String username;
        public String passwordHash;
        public String createdAt;
        public AccountState account;

        public UserState(String id, String name, String username, String passwordHash, String createdAt, AccountState account) {
            this.id = id;
            this.name = name;
            this.username = username;
            this.passwordHash = passwordHash;
            this.createdAt = createdAt;
            this.account = account;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", id);
            data.put("name", name);
            data.put("username", username);
            data.put("passwordHash", passwordHash);
            data.put("createdAt", createdAt);
            data.put("account", account.toMap());
            return data;
        }
    }

    public static class AccountState {
        public String accountNumber;
        public double balance;
        public String ownerId;
        public List<TransactionState> transactions = new ArrayList<>();

        public AccountState(String accountNumber, double balance, String ownerId) {
            this.accountNumber = accountNumber;
            this.balance = balance;
            this.ownerId = ownerId;
        }

        public boolean hasSufficientFunds(double amount) {
            return balance >= amount;
        }

        public TransactionState credit(double amount, String description, String fromAccount) {
            balance += amount;
            TransactionState transaction = new TransactionState(
                    "credit", amount, description, fromAccount, accountNumber, balance, nowIso());
            if (fromAccount == null) {
                transaction.type = "initial";
            }
            transactions.add(transaction);
            return transaction;
        }

        public TransactionState debit(double amount, String description, String toAccount) {
            balance -= amount;
            TransactionState transaction = new TransactionState(
                    "transfer-out", amount, description, accountNumber, toAccount, balance, nowIso());
            transactions.add(transaction);
            return transaction;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("accountNumber", accountNumber);
            data.put("balance", balance);
            data.put("ownerId", ownerId);
            data.put("transactions", transactions.stream().map(TransactionState::toMap).toList());
            return data;
        }
    }

    public static class TransactionState {
        public String id;
        public String type;
        public double amount;
        public String description;
        public String fromAccount;
        public String toAccount;
        public double balanceAfter;
        public String timestamp;

        public TransactionState(String type, double amount, String description, String fromAccount, String toAccount, double balanceAfter, String timestamp) {
            this.id = UUID.randomUUID().toString().substring(0, 8);
            this.type = type;
            this.amount = amount;
            this.description = description;
            this.fromAccount = fromAccount;
            this.toAccount = toAccount;
            this.balanceAfter = balanceAfter;
            this.timestamp = timestamp;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", id);
            data.put("type", type);
            data.put("amount", amount);
            data.put("description", description);
            data.put("fromAccount", fromAccount);
            data.put("toAccount", toAccount);
            data.put("balanceAfter", balanceAfter);
            data.put("timestamp", timestamp);
            return data;
        }
    }
}
