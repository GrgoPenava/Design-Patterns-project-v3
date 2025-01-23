package org.uzdiz.userInputChain;

import org.uzdiz.ConfigManager;
import org.uzdiz.memento.TicketCareTaker;
import org.uzdiz.memento.TicketMemento;
import org.uzdiz.utils.TableBuilder;

public class IKKPVCommand extends CommandHandlerChain {

    @Override
    protected boolean canHandle(String input) {
        return input.matches("^IKKPV(\\s*|\\s+\\d+)$");
    }

    @Override
    protected void execute(String input) {
        TicketCareTaker careTaker = ConfigManager.getInstance().getTicketCareTaker();

        if (input.trim().equals("IKKPV")) {
            if (careTaker.getMementoList().isEmpty()) {
                System.out.println("Nema spremljenih karti.");
                return;
            }

            TableBuilder table = new TableBuilder();
            table.setHeaders("Redni broj", "Oznaka vlaka", "Polazna stanica", "Odredišna stanica", "Datum", "Način kupovine", "Izvorna cijena", "Popust", "Konačna cijena", "Vrijeme polaska", "Vrijeme dolaska", "Vrijeme kupovine");

            for (int i = 0; i < careTaker.getMementoList().size(); i++) {
                TicketMemento memento = careTaker.getMemento(i);
                table.addRow(
                        String.valueOf(i + 1),
                        memento.getTicketDetails().getTicketOznakaVlaka(),
                        memento.getTicketDetails().getPolaznaStanica(),
                        memento.getTicketDetails().getOdredisnaStanica(),
                        memento.getTicketDetails().getDatum().toString(),
                        memento.getTicketDetails().getNacinKupovine(),
                        String.format("%.2f", memento.getTicketDetails().getIzvornaCijena()),
                        String.format("%.2f", memento.getTicketDetails().getPopustiIznos()),
                        String.format("%.2f", memento.getTicketDetails().getKonacnaCijena()),
                        memento.getTicketDetails().getVrijemePolaska(),
                        memento.getTicketDetails().getVrijemeDolaska(),
                        memento.getTicketDetails().getVrijemeKupovineKarte()
                );
            }

            table.build();
        } else {
            int ticketIndex;
            try {
                ticketIndex = Integer.parseInt(input.substring(6).trim()) - 1;
            } catch (NumberFormatException e) {
                ConfigManager.getInstance().incrementErrorCount();
                System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Redni broj karte mora biti cijeli broj.");
                return;
            }

            if (ticketIndex < 0 || ticketIndex >= careTaker.getMementoList().size()) {
                ConfigManager.getInstance().incrementErrorCount();
                System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Karta s rednim brojem " + (ticketIndex + 1) + " ne postoji.");
                return;
            }

            TicketMemento memento = careTaker.getMemento(ticketIndex);

            TableBuilder table = new TableBuilder();
            table.setHeaders("Redni broj", "Oznaka vlaka", "Polazna stanica", "Odredišna stanica", "Datum", "Način kupovine", "Izvorna cijena", "Popust", "Konačna cijena", "Vrijeme polaska", "Vrijeme dolaska", "Vrijeme kupovine");
            table.addRow(
                    String.valueOf(ticketIndex + 1),
                    memento.getTicketDetails().getTicketOznakaVlaka(),
                    memento.getTicketDetails().getPolaznaStanica(),
                    memento.getTicketDetails().getOdredisnaStanica(),
                    memento.getTicketDetails().getDatum().toString(),
                    memento.getTicketDetails().getNacinKupovine(),
                    String.format("%.2f", memento.getTicketDetails().getIzvornaCijena()),
                    String.format("%.2f", memento.getTicketDetails().getPopustiIznos()),
                    String.format("%.2f", memento.getTicketDetails().getKonacnaCijena()),
                    memento.getTicketDetails().getVrijemePolaska(),
                    memento.getTicketDetails().getVrijemeDolaska(),
                    memento.getTicketDetails().getVrijemeKupovineKarte()
            );

            table.build();
        }
    }
}