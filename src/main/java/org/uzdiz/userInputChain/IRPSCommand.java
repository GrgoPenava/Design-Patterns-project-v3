package org.uzdiz.userInputChain;

import org.uzdiz.ConfigManager;
import org.uzdiz.stationState.State;
import org.uzdiz.timeTableComposite.Etapa;
import org.uzdiz.timeTableComposite.StationComposite;
import org.uzdiz.timeTableComposite.TimeTableComponent;
import org.uzdiz.timeTableComposite.Train;
import org.uzdiz.utils.TableBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        String[] parts = input.split("\\s+");
        String status = parts[1];
        String oznakaPruge = parts.length > 2 ? parts[2] : null;

        List<String[]> rows = new ArrayList<>();
        Set<String> uniqueRelations = new HashSet<>();

        // Prolazak kroz vlakove i etape
        for (TimeTableComponent trainComponent : ConfigManager.getInstance().getVozniRed().getChildren()) {
            if (trainComponent instanceof Train train) {
                for (TimeTableComponent etapaComponent : train.getChildren()) {
                    if (etapaComponent instanceof Etapa etapa) {
                        if (oznakaPruge != null && !etapa.getOznakaPruge().equals(oznakaPruge)) {
                            continue;
                        }

                        List<StationComposite> stations = new ArrayList<>();
                        for (TimeTableComponent stationComponent : etapa.getChildren()) {
                            if (stationComponent instanceof StationComposite stationComposite) {
                                stations.add(stationComposite);
                            }
                        }

                        for (int i = 0; i < stations.size() - 1; i++) {
                            StationComposite currentStation = stations.get(i);
                            StationComposite nextStation = stations.get(i + 1);

                            boolean isReverse = isReverseDirection(currentStation, status);

                            // Provjeravamo stanje za trenutnu relaciju
                            State currentState = isReverse
                                    ? currentStation.getState(1) // Obrnuti smjer
                                    : currentStation.getState(0); // Normalni smjer

                            if (currentState != null && currentState.getStatus().equalsIgnoreCase(status)) {
                                // Konstrukcija relacije
                                String relation = isReverse
                                        ? nextStation.getNazivStanice() + " - " + currentStation.getNazivStanice() // Obrnuti smjer
                                        : currentStation.getNazivStanice() + " - " + nextStation.getNazivStanice(); // Normalni smjer

                                if (uniqueRelations.add(relation)) {
                                    rows.add(new String[]{
                                            etapa.getOznakaPruge(),
                                            train.getOznaka(),
                                            relation,
                                            "Kolosijek " + getKolosijekWithStatus(currentStation, status, isReverse),
                                            status
                                    });
                                }
                            }
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
            tableBuilder.setHeaders("Pruge", "Vlak", "Relacija", "Detalj kolosijeka", "Status");
            rows.forEach(tableBuilder::addRow);
            tableBuilder.build();
        }
    }

    private boolean validateInput(String input) {
        String regex = "^IRPS\\s+\\S+(\\s+\\S+)?$";
        return input.matches(regex);
    }

    private boolean isReverseDirection(StationComposite station, String status) {
        if (station.getBrojKolosjeka() == 2) {
            State reverseState = station.getState(1); // Stanje za obrnuti smjer
            return reverseState != null && reverseState.getStatus().equalsIgnoreCase(status);
        }
        return false; // Ako je broj kolosijeka 1, smjer nije obrnut
    }

    private int getKolosijekWithStatus(StationComposite station, String status, boolean isReverse) {
        if (station.getBrojKolosjeka() == 2) {
            return isReverse ? 2 : 1; // Obrnuti smjer => kolosijek 2, normalan smjer => kolosijek 1
        }
        return 1; // Ako je broj kolosijeka 1, uvijek je kolosijek 1
    }
}
