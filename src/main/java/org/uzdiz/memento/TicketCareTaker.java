package org.uzdiz.memento;

import java.util.ArrayList;
import java.util.List;

public class TicketCareTaker {
    private final List<TicketMemento> ticketHistory = new ArrayList<>();

    public void addMemento(TicketMemento memento) {
        ticketHistory.add(memento);
    }

    public TicketMemento getMemento(int index) {
        return ticketHistory.get(index);
    }

    public List<TicketMemento> getMementoList() {
        return ticketHistory;
    }
}
