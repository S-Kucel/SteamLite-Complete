package ticket;

import java.io.Serializable;
import java.time.LocalDateTime;

public final class TicketMessage implements Serializable {
    private final String author;
    private final String message;
    private final LocalDateTime createdAt;

    public TicketMessage(String author, String message) {
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("Author cannot be empty");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        this.author = author;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
