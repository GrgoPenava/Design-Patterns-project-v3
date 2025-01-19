package org.uzdiz.memento;

public class TicketMemento {
    private final TicketDetails ticketDetails;

    public TicketMemento(TicketDetails ticketDetails) {
        this.ticketDetails = ticketDetails;
    }

    public TicketDetails getTicketDetails() {
        return ticketDetails;
    }
}