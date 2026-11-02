package ticket;

import core.Customer;

/**
 * Pierwszy stan zgłoszenia – otwarte.
 */
public class OpenTicket extends Ticket {

    public OpenTicket(String id, String title, String description, String reporter) {
        super(id, title, description, reporter);
    }

    public OpenTicket(String id, String title, String description, Customer reporter) {
        super(id, title, description, reporter);
    }

    /**
     * Konstruktor kopiujący (teoretycznie pozwala wrócić do stanu "open",
     * ale w naszym przykładzie nie jest używany).
     */
    public OpenTicket(Ticket previous) {
        super(previous);
    }

    @Override
    public String getStatus() {
        return "OPEN";
    }
}
