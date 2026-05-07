package util;

import model.User;
import model.Transaction;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * FileHandler - Export Transaction History to .txt file
 */
public class FileHandler {

    public static void exportTransactions(User user, String outputPath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("=".repeat(80) + "\n");
            writer.write("                    CHAND & CO — TRANSACTION STATEMENT\n");
            writer.write("=".repeat(80) + "\n\n");

            writer.write("Customer Information:\n");
            writer.write("  Name           : " + user.getName() + "\n");
            writer.write("  Username       : " + user.getUsername() + "\n");
            writer.write("  Account Number : " + user.getAccountNumber() + "\n");
            writer.write("  Current Balance: PKR " + String.format("%.2f", user.getBalance()) + "\n");
            writer.write("  Account Created: " + user.getCreatedAt() + "\n");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            writer.write("  Report Generated: " + LocalDateTime.now().format(formatter) + "\n");

            writer.write("\n" + "=".repeat(80) + "\n");
            writer.write("TRANSACTION HISTORY\n");
            writer.write("=".repeat(80) + "\n\n");

            if (user.getAccount().getTransactions().isEmpty()) {
                writer.write("No transactions found.\n");
            } else {
                writer.write(String.format("%-20s %-15s %-30s %-15s %-15s%n",
                        "Date & Time", "Type", "Description", "Amount", "Balance After"));
                writer.write("-".repeat(95) + "\n");

                for (Transaction t : user.getAccount().getTransactions()) {
                    String desc = t.getDescription();
                    if (desc.length() > 28) {
                        desc = desc.substring(0, 28) + "..";
                    }

                    String amount = (t.isCredit() ? "+" : "-") + String.format("%.2f", t.getAmount());
                    writer.write(String.format("%-20s %-15s %-30s %-15s %-15.2f%n",
                            t.formatDate(),
                            t.getType(),
                            desc,
                            amount,
                            t.getBalanceAfter()));
                }
            }

            writer.write("\n" + "=".repeat(80) + "\n");
            writer.write("End of Statement\n");
            writer.write("=".repeat(80) + "\n");
        }
    }

    public static void exportUserReport(User user, String outputPath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("Chand & Co - User Account Report\n");
            writer.write("================================\n\n");
            writer.write(user.getDetails() + "\n\n");

            int transactionCount = user.getAccount().getTransactions().size();
            writer.write("Total Transactions: " + transactionCount + "\n");
            writer.write("Account Created: " + user.getCreatedAt() + "\n");
        }
    }
}
