package org.uzdiz.userInputChain;

import org.uzdiz.ConfigManager;
import org.uzdiz.stationState.*;
import org.uzdiz.timeTableComposite.Etapa;
import org.uzdiz.timeTableComposite.StationComposite;
import org.uzdiz.timeTableComposite.TimeTableComponent;
import org.uzdiz.timeTableComposite.Train;

import java.util.ArrayList;
import java.util.List;

public class PSP2SCommand extends CommandHandlerChain {

    @Override
    protected boolean canHandle(String input) {
        return input.matches("^PSP2S(\\s|$).*");
    }

    @Override
    protected void execute(String input) {
        if (!validateInput(input)) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() +
                    ": Neispravan format naredbe. Očekuje se format 'PSP2S oznaka - polaznaStanica - odredišnaStanica - status'.");
            return;
        }

        // Parsiranje ulaznog stringa
        String[] parts = input.substring(6).trim().split("\\s+-\\s+");
        String oznakaPruge = parts[0];
        String polaznaStanica = parts[1];
        String odredisnaStanica = parts[2];
        String status = parts[3];

        boolean etapaFound = false;
        boolean anyStationUpdated = false;

        // Dohvaćanje svih stanica s oznakom pruge iz Composite uzorka
        List<StationComposite> stationsOnRailway = getStationsFromComposite(oznakaPruge);

        // Pronalazak indeksa polazne i odredišne stanice u popisu stanica
        int indexPolazna = findStationIndex(stationsOnRailway, polaznaStanica);
        int indexOdredisna = findStationIndex(stationsOnRailway, odredisnaStanica);

        if (indexPolazna == -1 || indexOdredisna == -1) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() +
                    ": Polazna ili odredišna stanica ne postoji na pruzi '" + oznakaPruge + "'.");
            return;
        }

        // Određivanje smjera i ciljanih stanica
        boolean isNormalDirection = indexPolazna < indexOdredisna;
        List<StationComposite> targetStations = isNormalDirection
                ? stationsOnRailway.subList(indexPolazna, indexOdredisna + 1)
                : stationsOnRailway.subList(indexOdredisna, indexPolazna + 1);

        // Odredi stanje na temelju statusa
        State newState = determineState(status);
        if (newState == null) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() +
                    ": Nepoznati status: '" + status + "'.");
            return;
        }

        // Prolaz kroz vlakove i etape za ažuriranje stanja
        for (TimeTableComponent trainComponent : ConfigManager.getInstance().getVozniRed().getChildren()) {
            if (trainComponent instanceof Train train) {
                for (TimeTableComponent etapaComponent : train.getChildren()) {
                    if (etapaComponent instanceof Etapa etapa && etapa.getOznakaPruge().equals(oznakaPruge)) {
                        etapaFound = true;

                        // Ažuriranje stanja stanica unutar trenutne etape
                        for (StationComposite stationComposite : targetStations) {
                            if (etapa.getChildren().contains(stationComposite)) {
                                if (stationComposite.getBrojKolosjeka() == 1) {
                                    stationComposite.setState(0, newState);
                                } else if (stationComposite.getBrojKolosjeka() == 2) {
                                    if (isNormalDirection) {
                                        stationComposite.setState(0, newState); // Normalni smjer
                                    } else {
                                        stationComposite.setState(1, newState); // Obrnuti smjer
                                    }
                                }

                                System.out.println("Stanica je u kvaru. " + stationComposite.getNazivStanice());
                                anyStationUpdated = true;
                            }
                        }
                    }
                }
            }
        }

        // Ispisivanje poruka u konzolu na temelju rezultata
        if (!etapaFound) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() +
                    ": Etapa s oznakom pruge '" + oznakaPruge + "' nije pronađena.");
        } else if (!anyStationUpdated) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() +
                    ": Nije pronađen valjani raspon stanica između '" + polaznaStanica + "' i '" + odredisnaStanica +
                    "' za prugu '" + oznakaPruge + "'.");
        } else {
            System.out.println("Ažurirana stanja pruge između stanica '" + polaznaStanica + "' i '" +
                    odredisnaStanica + "' za prugu '" + oznakaPruge + "'.");
        }
    }

    private List<StationComposite> getStationsFromComposite(String oznakaPruge) {
        List<StationComposite> stations = new ArrayList<>();

        for (TimeTableComponent trainComponent : ConfigManager.getInstance().getVozniRed().getChildren()) {
            if (trainComponent instanceof Train train) {
                for (TimeTableComponent etapaComponent : train.getChildren()) {
                    if (etapaComponent instanceof Etapa etapa &&
                            etapa.getOznakaPruge().equals(oznakaPruge)) {
                        for (TimeTableComponent stationComponent : etapa.getChildren()) {
                            if (stationComponent instanceof StationComposite stationComposite) {
                                stations.add(stationComposite);
                            }
                        }
                    }
                }
            }
        }

        return stations;
    }

    private int findStationIndex(List<StationComposite> stations, String stationName) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.get(i).getNazivStanice().equals(stationName)) {
                return i;
            }
        }
        return -1;
    }

    private boolean validateInput(String input) {
        String regex = "^PSP2S\\s+\\S+\\s+-\\s+.+?\\s+-\\s+.+?\\s+-\\s+\\S+$";
        return input.matches(regex);
    }

    private State determineState(String status) {
        return switch (status.toUpperCase()) {
            case "K" -> new KvarState();
            case "T" -> new TestiranjeState();
            case "I" -> new IspravnaState();
            case "Z" -> new ZatvorenaState();
            default -> null;
        };
    }
}