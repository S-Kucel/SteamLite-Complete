package ticket;

/**
 * Zgłoszenie zamknięte – zawiera podsumowanie rozwiązania.
 */
public class ClosedTicket extends Ticket {

    private String resolutionSummary;
    private boolean solved;   // czy problem faktycznie rozwiązany

    public ClosedTicket(Ticket previous, String resolutionSummary, boolean solved) {
        super(previous);    // kopiujemy dane wspólne

        if (resolutionSummary == null || resolutionSummary.isBlank()) {
            throw new IllegalArgumentException("Resolution summary cannot be empty");
        }
        this.resolutionSummary = resolutionSummary;
        this.solved = solved;
    }

    public String getResolutionSummary() {
        return resolutionSummary;
    }

    public boolean isSolved() {
        return solved;
    }

    @Override
    public String getStatus() {
        return "CLOSED";
    }

    @Override
    public String toString() {
        return super.toString() +
                ", solved=" + solved +
                ", resolution='" + resolutionSummary + "'";
    }
}
