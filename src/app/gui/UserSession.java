package app.gui;

import core.CreatorCustomer;
import core.Customer;
import core.IGamePublisher;
import core.TechSupport;

// Przechowuje zalogowana osobe i role w GUI.
public final class UserSession {
    private final Customer customer;
    private final IGamePublisher publisher;
    private final TechSupport support;

    public UserSession(Customer customer) {
        this.customer = customer;
        // Wykrycie roli wydawcy (CreatorCustomer implementuje IGamePublisher).
        this.publisher = customer instanceof IGamePublisher ? (IGamePublisher) customer : null;
        this.support = null;
    }

    public UserSession(TechSupport support) {
        this.customer = null;
        this.publisher = null;
        this.support = support;
    }

    public Customer getCustomer() {
        return customer;
    }

    public boolean isDeveloper() {
        return publisher != null;
    }

    public IGamePublisher getPublisher() {
        return publisher;
    }

    public boolean isSupport() {
        return support != null;
    }

    public TechSupport getSupport() {
        return support;
    }

    // nazwa roli do paska statusu.
    public String getRoleLabel() {
        if (support != null) {
            return "TechSupport";
        }
        if (customer instanceof CreatorCustomer) {
            return "Creator";
        }
        return "Customer";
    }
}
