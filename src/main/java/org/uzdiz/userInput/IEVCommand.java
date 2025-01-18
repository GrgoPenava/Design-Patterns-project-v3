package org.uzdiz.userInput;

import org.uzdiz.ConfigManager;
import org.uzdiz.DrivingDays;
import org.uzdiz.builder.Station;
import org.uzdiz.railwayFactory.Railway;
import org.uzdiz.timeTableComposite.*;
import org.uzdiz.utils.TableBuilder;

import java.util.List;
import java.util.Optional;

public class IEVCommand extends CommandHandlerChain {

    @Override
    protected boolean canHandle(String input) {
        return input.matches("^IEV(\\s|$).*");
    }

    @Override
    public void execute(String input) {
        ConfigManager config = ConfigManager.getInstance();

        if (!validateInput(input)) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Neispravna sintaksa. Ispravan oblik: IEV oznaka (npr. 'IEV broj').");
            return;
        }

        String oznakaVlaka = input.substring(4).trim();

        TimeTableComposite vozniRed = ConfigManager.getInstance().getVozniRed();

        if (vozniRed == null || vozniRed.getChildren().isEmpty()) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Nema dostupnih podataka o voznom redu.");
            return;
        }

        Train train = findTrainByOznaka(vozniRed, oznakaVlaka);

        if (train == null) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Vlak s oznakom '" + oznakaVlaka + "' nije pronađen.");
            return;
        }

        TableBuilder table = new TableBuilder();
        table.setHeaders("Oznaka vlaka", "Oznaka pruge", "Polazišna stanica", "Odredišna stanica",
                "Vrijeme polaska", "Vrijeme dolaska", "Ukupan broj km", "Dani u tjednu");

        processTrainStages(train, table);

        table.build();
    }

    private boolean validateInput(String input) {
        return input.startsWith("IEV") && input.length() > 4;
    }

    private Train findTrainByOznaka(TimeTableComposite vozniRed, String oznakaVlaka) {
        for (TimeTableComponent component : vozniRed.getChildren()) {
            if (component instanceof Train && ((Train) component).getOznaka().equals(oznakaVlaka)) {
                return (Train) component;
            }
        }
        return null;
    }

    private void processTrainStages(Train train, TableBuilder table) {
        String oznakaVlaka = train.getOznaka();

        for (TimeTableComponent etapaComponent : train.getChildren()) {
            if (etapaComponent instanceof Etapa) {
                Etapa etapa = (Etapa) etapaComponent;

                String oznakaPruge = etapa.getOznakaPruge();
                String polaznaStanica = etapa.getPocetnaStanica();
                String odredisnaStanica = etapa.getOdredisnaStanica();
                String vrijemePolaska = etapa.getVrijemePolaska();
                String vrijemeDolaska = calculateArrivalTime(vrijemePolaska, etapa.getTrajanjeVoznje());
                int ukupanBrojKm = calculateTotalDistance(etapa);
                String daniUTjednu = getDrivingDays(etapa.getOznakaDana());

                table.addRow(oznakaVlaka, oznakaPruge, polaznaStanica, odredisnaStanica,
                        vrijemePolaska, vrijemeDolaska, String.valueOf(ukupanBrojKm), daniUTjednu);
            }
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

        if (startIndex == -1 || endIndex == -1) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Stanica nije pronađena na pruzi.");
            return 0;
        }

        if ("O".equals(etapa.getSmjer())) {
            if (startIndex < endIndex) {
                return 0;
            }
            for (int i = startIndex; i > endIndex; i--) {
                totalDistance += stations.get(i).getDuzina();
            }
        } else {
            if (startIndex > endIndex) {
                return 0;
            }
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


    private String getDrivingDays(String oznakaDana) {
        if (oznakaDana == null) {
            return "Po, U, Sr, Č, Pe, Su, N";
        }

        Optional<DrivingDays> drivingDays = ConfigManager.getInstance().getDrivingDays().stream()
                .filter(days -> days.getOznaka().equals(oznakaDana))
                .findFirst();

        return drivingDays.map(days -> String.join(", ", days.getDays()))
                .orElse("-");
    }
}

