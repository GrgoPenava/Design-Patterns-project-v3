package org.uzdiz.userInputChain;

import org.uzdiz.ConfigManager;
import org.uzdiz.builder.Station;
import org.uzdiz.stationState.*;
import org.uzdiz.timeTableComposite.Etapa;
import org.uzdiz.timeTableComposite.StationComposite;
import org.uzdiz.timeTableComposite.TimeTableComponent;
import org.uzdiz.timeTableComposite.Train;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

        String[] parts = input.substring(6).trim().split("\\s+-\\s+");
        String oznakaPruge = parts[0];
        String polaznaStanica = parts[1];
        String odredisnaStanica = parts[2];
        String status = parts[3];

        boolean etapaFound = false;
        boolean anyStationUpdated = false;

        State newState = determineState(status);
        if (newState == null) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() +
                    ": Nepoznati status: '" + status + "'.");
            return;
        }

        List<Station> stationsOnRailway = ConfigManager.getInstance().getStations().stream()
                .filter(station -> oznakaPruge.equals(station.getOznakaPruge()))
                .collect(Collectors.toList());

        int indexPolazna = findStationIndex(stationsOnRailway, polaznaStanica);
        int indexOdredisna = findStationIndex(stationsOnRailway, odredisnaStanica);

        if (indexPolazna == -1 || indexOdredisna == -1) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() +
                    ": Polazna ili odredišna stanica ne postoji na pruzi '" + oznakaPruge + "'.");
            return;
        }

        boolean isNormalDirection = indexPolazna < indexOdredisna;
        if (!isNormalDirection) {
            int temp = indexPolazna;
            indexPolazna = indexOdredisna;
            indexOdredisna = temp;
        }

        List<Station> targetStations = stationsOnRailway.subList(indexPolazna, indexOdredisna + 1);

        for (TimeTableComponent trainComponent : ConfigManager.getInstance().getVozniRed().getChildren()) {
            if (trainComponent instanceof Train train) {
                for (TimeTableComponent etapaComponent : train.getChildren()) {
                    if (etapaComponent instanceof Etapa etapa &&
                            etapa.getOznakaPruge().equals(oznakaPruge)) {

                        etapaFound = true;
                        List<StationComposite> stationsInEtapa = getMatchingStations(etapa, targetStations);

                        for (StationComposite stationComposite : stationsInEtapa) {
                            if (stationComposite.getBrojKolosjeka() == 1) {
                                stationComposite.setState(0, newState);
                                this.setStationState(stationComposite.getIdStanice(), 0, newState);

                            } else if (stationComposite.getBrojKolosjeka() == 2) {
                                if (!etapa.getSmjer().equals("O")) {
                                    stationComposite.setState(0, newState);
                                    this.setStationState(stationComposite.getIdStanice(), 0, newState);

                                } else {
                                    stationComposite.setState(1, newState);
                                    this.setStationState(stationComposite.getIdStanice(), 1, newState);
                                }
                            }
                            anyStationUpdated = true;
                        }
                    }
                }
            }
        }

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

    private List<StationComposite> getMatchingStations(Etapa etapa, List<Station> targetStations) {
        List<StationComposite> result = new ArrayList<>();
        for (TimeTableComponent stationComponent : etapa.getChildren()) {
            if (stationComponent instanceof StationComposite stationComposite) {
                boolean matchesTarget = targetStations.stream()
                        .anyMatch(targetStation -> targetStation.getNaziv().equals(stationComposite.getNazivStanice()));

                if (matchesTarget) {
                    result.add(stationComposite);
                }
            }
        }
        return result;
    }

    private int findStationIndex(List<Station> stations, String stationName) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.get(i).getNaziv().equals(stationName)) {
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

    private void setStationState(Integer id, Integer kolosjek, State newState) {
        ConfigManager.getInstance().setStationState(id, kolosjek, newState);
    }
}