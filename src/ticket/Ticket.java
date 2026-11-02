package ticket;

import core.Customer;
import core.ObjectPlus;
import core.TechSupport;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dziedziczenie dynamiczne (dynamic) – nadklasa Ticket.
 *
 * Obiekt zgłoszenia może „zmieniać klasę” w trakcie życia:
 * OpenTicket -> InProgressTicket -> ClosedTicket.
 *
 * - wspólne dane są w nadklasie,
 * - każda podklasa ma konstruktor kopiujący poprzedni obiekt.
 */
public abstract class Ticket extends ObjectPlus {

    // wspólne inwarianty
    protected String id;
    protected String title;
    protected String description;
    protected String reporter;        // kto zgłasza (np. login użytkownika)
    protected Customer reporterCustomer;
    protected TechSupport assigneeSupport;
    protected LocalDate createdAt;
    protected LocalDate lastUpdated;
    protected List<TicketMessage> messages = new ArrayList<>();

    /**
     * Podstawowy konstruktor – pierwszy „stan” zgłoszenia.
     */
    protected Ticket(String id, String title, String description, String reporter) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Ticket id cannot be empty");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Ticket title cannot be empty");
        }
        if (reporter == null || reporter.isBlank()) {
            throw new IllegalArgumentException("Reporter cannot be empty");
        }

        this.id = id;
        this.title = title;
        this.description = (description != null ? description : "");
        this.reporter = reporter;
        this.createdAt = LocalDate.now();
        this.lastUpdated = this.createdAt;
    }

    protected Ticket(String id, String title, String description, Customer reporter) {
        this(id, title, description, reporter != null ? reporter.getUsername() : null);
        if (reporter == null) {
            throw new IllegalArgumentException("Reporter cannot be null");
        }
        this.reporterCustomer = reporter;
    }

    /**
     * Konstruktor kopiujący – używany przy „zmianie klasy”.
     * Kopiuje wspólne dane z poprzedniego obiektu Ticket.
     */
    protected Ticket(Ticket previous) {
        if (previous == null) {
            throw new IllegalArgumentException("Previous ticket cannot be null");
        }
        this.id = previous.id;
        this.title = previous.title;
        this.description = previous.description;
        this.reporter = previous.reporter;
        this.reporterCustomer = previous.reporterCustomer;
        this.assigneeSupport = previous.assigneeSupport;
        this.createdAt = previous.createdAt;
        this.lastUpdated = LocalDate.now();
        this.messages = new ArrayList<>(previous.messages);
        previous.removeFromExtent();
    }

    /**
     * Abstrakcyjny „status” – każda podklasa zwraca swój.
     */
    public abstract String getStatus();

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getReporter() {
        return reporter;
    }

    public Customer getReporterCustomer() {
        return reporterCustomer;
    }

    public TechSupport getAssigneeSupport() {
        return assigneeSupport;
    }

    protected void setAssigneeSupport(TechSupport support) {
        this.assigneeSupport = support;
    }


    public String getDescription() {
        return description;
    }

    public List<TicketMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void addMessage(String author, String message) {
        messages.add(new TicketMessage(author, message));
        lastUpdated = LocalDate.now();
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public LocalDate getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public String toString() {
        return String.format(
                "%s{id='%s', title='%s', reporter='%s', status=%s, createdAt=%s, lastUpdated=%s}",
                getClass().getSimpleName(), id, title, reporter, getStatus(), createdAt, lastUpdated
        );
    }
}
