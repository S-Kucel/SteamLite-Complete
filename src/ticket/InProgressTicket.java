package ticket;

import core.TechSupport;

/**
 * "Zgłoszenie w trakcie obsługi" przypisane do agenta supportu.
 */
public class InProgressTicket extends Ticket {

    private String assignee;  // np. login pracownika supportu
    private TechSupport assigneeSupport;

    public InProgressTicket(Ticket previous, String assignee) {
        super(previous);   // kopiowanie wspólnych danych

        if (assignee == null || assignee.isBlank()) {
            throw new IllegalArgumentException("Assignee cannot be empty");
        }
        this.assignee = assignee;
    }

    public InProgressTicket(Ticket previous, TechSupport assignee) {
        super(previous);

        if (assignee == null) {
            throw new IllegalArgumentException("Assignee cannot be null");
        }
        this.assigneeSupport = assignee;
        this.assignee = assignee.getUsername();
        setAssigneeSupport(assignee);
    }

    public String getAssignee() {
        return assignee;
    }

    public TechSupport getAssigneeSupport() {
        return assigneeSupport;
    }

    @Override
    public String getStatus() {
        return "IN_PROGRESS";
    }

    @Override
    public String toString() {
        return super.toString() + ", assignee='" + assignee + "'";
    }
}
