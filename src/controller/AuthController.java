package controller;

import model.User;
import model.Admin;
import service.BankService;
import exception.UserNotFoundException;

/**
 * AuthController - Handles Login and Registration
 */
public class AuthController {
    private final BankService bankService;

    public AuthController(BankService bankService) {
        this.bankService = bankService;
    }

    public User registerUser(String name, String username, String password, double initialDeposit) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (initialDeposit < 0) {
            throw new IllegalArgumentException("Initial deposit cannot be negative");
        }

        return bankService.registerUser(name, username, password, initialDeposit);
    }

    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        return bankService.loginUser(username, password);
    }

    public Admin adminLogin(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        return bankService.loginAdmin(username, password);
    }

    public User getUserInfo(String username) {
        return bankService.getUser(username);
    }
}
