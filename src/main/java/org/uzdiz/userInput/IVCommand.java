package org.uzdiz.userInput;

import org.uzdiz.ConfigManager;
import org.uzdiz.timeTableComposite.*;
import org.uzdiz.utils.TableBuilder;
import org.uzdiz.builder.Station;
import org.uzdiz.railwayFactory.Railway;

import java.util.List;

public class IVCommand implements Command {

    @Override
    public void execute(String input) {
        ConfigManager config = ConfigManager.getInstance();
        TimeTableComposite vozniRed = ConfigManager.getInstance().getVozniRed();

        if (vozniRed == null || vozniRed.getChildren().isEmpty()) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Nema dostupnih podataka o vlakovima.");
            return;
        }

        TableBuilder table = new TableBuilder();
        table.setHeaders("Oznaka vlaka", "Polazišna stanica", "Odredišna stanica", "Vrijeme polaska", "Vrijeme dolaska", "Ukupan broj km");

        for (TimeTableComponent trainComponent : vozniRed.getChildren()) {
            if (trainComponent instanceof Train) {
                Train train = (Train) trainComponent;
                processTrain(train, table);
            }
        }

        table.build();
    }

    private void processTrain(Train train, TableBuilder table) {
        String oznakaVlaka = train.getOznaka();

        String polaznaStanica = null;
        String odredisnaStanica = null;
        String vrijemePolaska = null;
        String vrijemeDolaska = null;
        int ukupnoKm = 0;

        for (TimeTableComponent etapaComponent : train.getChildren()) {
            if (etapaComponent instanceof Etapa) {
                Etapa etapa = (Etapa) etapaComponent;

                if (polaznaStanica == null) {
                    polaznaStanica = etapa.getPocetnaStanica();
                    vrijemePolaska = etapa.getVrijemePolaska();
                }

                odredisnaStanica = etapa.getOdredisnaStanica();
                vrijemeDolaska = calculateArrivalTime(etapa.getVrijemePolaska(), etapa.getTrajanjeVoznje());
                ukupnoKm += calculateTotalDistance(etapa);
            }
        }

        if (polaznaStanica != null && odredisnaStanica != null) {
            table.addRow(oznakaVlaka, polaznaStanica, odredisnaStanica, vrijemePolaska, vrijemeDolaska, String.valueOf(ukupnoKm));
        }
    }


    private String calculateArrivalTime(String vrijemePolaska, String trajanjeVoznje) {
        try {
            String[] polazakParts = vrijemePolaska.split(":");
            String[] trajanjeParts = trajanjeVoznje.split(":");

            int polazakSati = Integer.parseInt(polazakParts[0]);
            int polazakMinute = Integer.parseInt(polazakParts[1]);

            int trajanjeSati = Integer.parseInt(trajanjeParts[0]);
            int trajanjeMinute = Integer.parseInt(trajanjeParts[1]);

            int ukupnoMinute = polazakMinute + trajanjeMinute;
            int ukupnoSati = polazakSati + trajanjeSati + ukupnoMinute / 60;
            ukupnoMinute %= 60;

            ukupnoSati %= 24;

            return String.format("%02d:%02d", ukupnoSati, ukupnoMinute);
        } catch (Exception e) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Neispravan format vremena polaska ili trajanja vožnje.");
            return "//";
        }
    }

    private int calculateTotalDistance(Etapa etapa) {
        int totalDistance = 0;

        Railway railway = ConfigManager.getInstance().getRailwayByOznakaPruge(etapa.getOznakaPruge());
        if (railway == null) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Pruga s oznakom '" + etapa.getOznakaPruge() + "' nije pronađena.");
            return 0;
        }

        List<Station> stations = railway.getPopisSvihStanica();
        int startIndex = findStationIndex(stations, etapa.getPocetnaStanica());
        int endIndex = findStationIndex(stations, etapa.getOdredisnaStanica());

        if (startIndex == -1) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Polazna stanica '" + etapa.getPocetnaStanica() + "' nije pronađena na pruzi.");
            return 0;
        }

        if (endIndex == -1) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Odredišna stanica '" + etapa.getOdredisnaStanica() + "' nije pronađena na pruzi.");
            return 0;
        }

        if ("O".equals(etapa.getSmjer())) {
            for (int i = startIndex; i > endIndex; i--) {
                totalDistance += stations.get(i).getDuzina();
            }
        } else {
            for (int i = startIndex + 1; i <= endIndex; i++) {
                totalDistance += stations.get(i).getDuzina();
            }
        }

        return totalDistance;
    }

    private int findStationIndex(List<Station> stations, String stationName) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.get(i).getNaziv().equals(stationName)) {
                return i;
            }
        }
        return -1;
    }
}
