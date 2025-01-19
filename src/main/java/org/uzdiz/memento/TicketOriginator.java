package org.uzdiz.memento;

public class TicketOriginator {
    private TicketDetails ticketDetails;

    public void setDetails(TicketDetails ticketDetails) {
        this.ticketDetails = ticketDetails;
    }

    public TicketDetails getDetails() {
        return ticketDetails;
    }

    public TicketMemento saveToMemento() {
        return new TicketMemento(ticketDetails);
    }

    public void restoreFromMemento(TicketMemento memento) {
        this.ticketDetails = memento.getTicketDetails();
    }
}
