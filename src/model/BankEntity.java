package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Abstract base class for all bank entities
 * Demonstrates: ABSTRACTION
 */
public abstract class BankEntity {
    private final String id;
    private final String name;
    private final LocalDateTime createdAt;

    public BankEntity(String id, String name) {
        this.id = id;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreatedAt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return createdAt.format(formatter);
    }

    // Abstract method — forces subclasses to implement
    public abstract String getDetails();
}
