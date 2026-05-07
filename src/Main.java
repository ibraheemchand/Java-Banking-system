import controller.AuthController;
import controller.UserController;
import controller.AdminController;
import model.User;
import model.Admin;
import service.BankService;

/**
 * Main Entry Point for Chand & Co Application
 * Demonstrates the complete backend system
 */
public class Main {
    public static void main(String[] args) {
        // Initialize the singleton service
        BankService bankService = BankService.getInstance();

        // Initialize controllers
        AuthController authController = new AuthController(bankService);
        UserController userController = new UserController(bankService);
        AdminController adminController = new AdminController(bankService);

        System.out.println("=".repeat(70));
        System.out.println("        CHAND & CO — Banking Management System (Java Backend)");
        System.out.println("=".repeat(70));
        System.out.println();

        // Demo: User Login
        System.out.println("--- USER LOGIN DEMO ---");
        try {
            User user = authController.login("alice", "pass123");
            System.out.println("✓ Logged in: " + user.getDetails());
            System.out.println("  Balance: PKR " + userController.getBalance("alice"));
        } catch (Exception e) {
            System.out.println("✗ Login failed: " + e.getMessage());
        }
        System.out.println();

        // Demo: Money Transfer
        System.out.println("--- TRANSFER DEMO ---");
        try {
            var result = userController.transferMoney("alice", "bob", 500, "Gift for Bob");
            System.out.println("✓ " + result);
        } catch (Exception e) {
            System.out.println("✗ Transfer failed: " + e.getMessage());
        }
        System.out.println();

        // Demo: View Balance
        System.out.println("--- BALANCE CHECK ---");
        try {
            double balance = userController.getBalance("alice");
            System.out.println("Alice's Balance: PKR " + String.format("%.2f", balance));

            balance = userController.getBalance("bob");
            System.out.println("Bob's Balance: PKR " + String.format("%.2f", balance));
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
        System.out.println();

        // Demo: Admin Access
        System.out.println("--- ADMIN LOGIN DEMO ---");
        try {
            Admin admin = authController.adminLogin("admin", "admin123");
            System.out.println("✓ " + admin.getDetails());

            System.out.println("\nAdmin Statistics:");
            System.out.println("  Total Users: " + adminController.getTotalUsers());
            System.out.println("  Total Balance in System: PKR " + 
                    String.format("%.2f", adminController.getTotalBalance()));
            System.out.println("  Average Balance: PKR " + 
                    String.format("%.2f", adminController.getAverageBalance()));
            System.out.println("  Total Transactions: " + adminController.getActiveTransactions());
        } catch (Exception e) {
            System.out.println("✗ Admin login failed: " + e.getMessage());
        }
        System.out.println();

        // Demo: Transaction History
        System.out.println("--- TRANSACTION HISTORY ---");
        try {
            var transactions = userController.getRecentTransactions("alice", 3);
            System.out.println("Alice's Recent Transactions:");
            for (var t : transactions) {
                System.out.println("  " + t);
            }
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
        System.out.println();

        // Demo: Export Transactions
        System.out.println("--- EXPORT DEMO ---");
        try {
            User alice = bankService.getUser("alice");
            String outputPath = "alice_transactions.txt";
            util.FileHandler.exportTransactions(alice, outputPath);
            System.out.println("✓ Transactions exported to: " + outputPath);
        } catch (Exception e) {
            System.out.println("✗ Export failed: " + e.getMessage());
        }
        System.out.println();

        System.out.println("=".repeat(70));
        System.out.println("Demo completed successfully!");
        System.out.println("=".repeat(70));
    }
}
