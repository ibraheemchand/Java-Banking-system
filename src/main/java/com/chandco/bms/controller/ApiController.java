package com.chandco.bms.controller;

import com.chandco.bms.service.BankStateService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class ApiController {
    private final BankStateService bankStateService;

    public ApiController(BankStateService bankStateService) {
        this.bankStateService = bankStateService;
    }

    @GetMapping("/state")
    public Map<String, Object> state() {
        return bankStateService.snapshot();
    }

    @GetMapping("/transactions")
    public List<Map<String, Object>> transactions() {
        return bankStateService.allTransactions();
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, Object>> transfer(@RequestBody TransferRequest request) {
        return ResponseEntity.ok(bankStateService.transfer(
                request.fromUsername(),
                request.toUsername(),
                request.amount(),
                request.note()));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(bankStateService.register(
                request.fullName(),
                request.username(),
                request.password(),
                request.initialDeposit()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(bankStateService.resetPassword(
                request.adminUsername(),
                request.targetUsername(),
                request.newPassword()));
    }

    public record TransferRequest(String fromUsername, String toUsername, double amount, String note) { }

    public record RegisterRequest(String fullName, String username, String password, double initialDeposit) { }

    public record ResetPasswordRequest(String adminUsername, String targetUsername, String newPassword) { }
}
