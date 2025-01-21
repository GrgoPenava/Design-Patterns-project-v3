package org.uzdiz.userInputChain;

import org.uzdiz.ConfigManager;
import org.uzdiz.stationState.State;
import org.uzdiz.timeTableComposite.Etapa;
import org.uzdiz.timeTableComposite.StationComposite;
import org.uzdiz.timeTableComposite.TimeTableComponent;
import org.uzdiz.timeTableComposite.Train;
import org.uzdiz.utils.TableBuilder;

import java.util.ArrayList;
import java.util.List;

public class IRPSCommand extends CommandHandlerChain {

    @Override
    protected boolean canHandle(String input) {
        return input.matches("^IRPS(\\s|$).*");
    }

    @Override
    protected void execute(String input) {
        // Validacija ulaznog formata
        if (!validateInput(input)) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() +
                    ": Neispravan format naredbe. Očekuje se format 'IRPS status [oznaka]'.");
            return;
        }

        // Parsiranje ulaznog stringa
        String[] parts = input.split("\\s+");
        String status = parts[1];
        String oznakaPruge = parts.length > 2 ? parts[2] : null;

        // Lista redaka za tablicu
        List<String[]> rows = new ArrayList<>();

        // Prolazak kroz vlakove i etape
        for (TimeTableComponent trainComponent : ConfigManager.getInstance().getVozniRed().getChildren()) {
            if (trainComponent instanceof Train train) {
                for (TimeTableComponent etapaComponent : train.getChildren()) {
                    if (etapaComponent instanceof Etapa etapa) {
                        // Ako je oznaka pruge zadana, filtriraj po njoj
                        if (oznakaPruge != null && !etapa.getOznakaPruge().equals(oznakaPruge)) {
                            continue;
                        }

                        boolean etapaHasStatus = false;

                        // Prolazak kroz sve stanice u etapi
                        for (TimeTableComponent stationComponent : etapa.getChildren()) {
                            if (stationComponent instanceof StationComposite stationComposite) {
                                // Provjera svih kolosijeka stanice
                                for (int i = 0; i < stationComposite.getBrojKolosjeka(); i++) {
                                    State currentState = stationComposite.getState(i);
                                    if (currentState != null && currentState.getStatus().equalsIgnoreCase(status)) {
                                        etapaHasStatus = true;
                                        // Dodavanje retka u tablicu
                                        rows.add(new String[]{
                                                etapa.getOznakaPruge(),
                                                train.getOznaka(),
                                                stationComposite.getNazivStanice(),
                                                "Kolosijek " + (i + 1),
                                                currentState.getStatus()
                                        });
                                    }
                                }
                            }
                        }

                        // Ako etapa sadrži barem jednu stanicu u traženom statusu, dodaje se u tablicu
                        if (etapaHasStatus) {
                            System.out.println("Etapa s oznakom '" + etapa.getOznakaPruge() +
                                    "' sadrži stanice u statusu '" + status + "'.");
                        }
                    }
                }
            }
        }

        // Generiranje i ispis tablice
        if (rows.isEmpty()) {
            System.out.println("Nema rezultata za status '" + status + "'" +
                    (oznakaPruge != null ? " na pruzi '" + oznakaPruge + "'." : "."));
        } else {
            TableBuilder tableBuilder = new TableBuilder();
            tableBuilder.setHeaders("Pruge", "Vlak", "Stanica", "Detalj kolosijeka", "Status");
            rows.forEach(tableBuilder::addRow);
            tableBuilder.build();
        }
    }

    private boolean validateInput(String input) {
        String regex = "^IRPS\\s+\\S+(\\s+\\S+)?$";
        return input.matches(regex);
    }
}