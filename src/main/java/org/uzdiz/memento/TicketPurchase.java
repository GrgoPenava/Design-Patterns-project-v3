package org.uzdiz.memento;

import org.uzdiz.ConfigManager;
import org.uzdiz.strategy.PriceCalculationStrategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TicketPurchase {
    private final PriceCalculationStrategy strategy;
    private final TicketOriginator originator;
    private final TicketCareTaker careTaker = ConfigManager.getInstance().getTicketCareTaker();

    public TicketPurchase(PriceCalculationStrategy strategy, TicketOriginator originator) {
        this.strategy = strategy;
        this.originator = originator;
    }

    public void purchaseTicket(String trainNumber, String fromStation, String toStation, LocalDate date, double basePrice, boolean isWeekend, String nacinKupovine, String vrijemePolaska, String vrijemeDolaska) {
        double finalPrice = strategy.calculatePrice(basePrice, isWeekend);

        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm");

        TicketDetails ticketDetails = new TicketDetails(trainNumber, fromStation, toStation, date, nacinKupovine, finalPrice, vrijemePolaska, vrijemeDolaska, currentDateTime.format(formatter));

        originator.setDetails(ticketDetails);
        careTaker.addMemento(originator.saveToMemento());
    }

    public void printPurchaseHistory() {
        for (TicketMemento memento : careTaker.getMementoList()) {
            System.out.println(memento.getTicketDetails());
        }
    }
}
