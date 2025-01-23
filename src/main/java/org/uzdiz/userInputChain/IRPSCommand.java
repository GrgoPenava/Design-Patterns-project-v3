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

        // Parsiranje ulaznog stringa
        String[] parts = input.split("\\s+");
        String status = parts[1];
        String oznakaPruge = parts.length > 2 ? parts[2] : null;

        // Lista redaka za tablicu
        List<String[]> rows = new ArrayList<>();
        Set<String> uniqueRelations = new HashSet<>(); // Za praćenje jedinstvenih relacija

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
                        List<StationComposite> stationsInEtapa = new ArrayList<>();

                        // Prolazak kroz sve stanice u etapi
                        for (TimeTableComponent stationComponent : etapa.getChildren()) {
                            if (stationComponent instanceof StationComposite stationComposite) {
                                // Provjera svih kolosijeka stanice
                                for (int i = 0; i < stationComposite.getBrojKolosjeka(); i++) {
                                    State currentState = stationComposite.getState(i);
                                    if (currentState != null && currentState.getStatus().equalsIgnoreCase(status)) {
                                        etapaHasStatus = true;
                                        stationsInEtapa.add(stationComposite);
                                    }
                                }
                            }
                        }

                        // Kreiranje relacija između stanica u etapi s odgovarajućim statusom
                        for (int i = 0; i < stationsInEtapa.size() - 1; i++) {
                            StationComposite currentStation = stationsInEtapa.get(i);
                            StationComposite nextStation = stationsInEtapa.get(i + 1);

                            // Provjera ako su stanice identične ili stanje Kvar je u obrnutom smjeru
                            if (currentStation.getNazivStanice().equals(nextStation.getNazivStanice())) {
                                continue;
                            }

                            String relation;
                            if (isReverseDirection(currentStation, nextStation, status)) {
                                if (etapa.getSmjer().equals("O")) {
                                    relation = currentStation.getNazivStanice() + " - " + nextStation.getNazivStanice();
                                } else {
                                    relation = nextStation.getNazivStanice() + " - " + currentStation.getNazivStanice();
                                }
                            } else {
                                relation = currentStation.getNazivStanice() + " - " + nextStation.getNazivStanice();
                            }

                            if (uniqueRelations.add(relation)) { // Provjera i dodavanje samo ako relacija ne postoji
                                rows.add(new String[]{
                                        etapa.getOznakaPruge(),
                                        train.getOznaka(),
                                        relation,
                                        "Kolosijek " + (getKolosijekWithStatus(currentStation, nextStation, status)),
                                        status
                                });
                            }
                        }

                        // Ako etapa sadrži barem jednu stanicu u traženom statusu
                        if (etapaHasStatus) {
                            /*System.out.println("Etapa s oznakom '" + etapa.getOznakaPruge() +
                                    "' sadrži stanice u statusu '" + status + "'.");*/
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

    private boolean isReverseDirection(StationComposite current, StationComposite next, String status) {
        for (int i = 0; i < next.getBrojKolosjeka(); i++) {
            State state = next.getState(i);
            if (state != null && state.getStatus().equalsIgnoreCase(status)) {
                return i != 0; // Obrnuti smjer ako je stanje u drugom kolosijeku
            }
        }
        return false;
    }

    private int getKolosijekWithStatus(StationComposite current, StationComposite next, String status) {
        for (int i = 0; i < current.getBrojKolosjeka(); i++) {
            State state = current.getState(i);
            if (state != null && state.getStatus().equalsIgnoreCase(status)) {
                return i + 1;
            }
        }
        return 1; // Default na prvi kolosijek ako nije specificirano
    }
}
