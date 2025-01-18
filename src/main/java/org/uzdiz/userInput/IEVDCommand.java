package org.uzdiz.userInput;

import org.uzdiz.ConfigManager;
import org.uzdiz.DrivingDays;
import org.uzdiz.timeTableComposite.*;
import org.uzdiz.utils.TableBuilder;

import java.util.*;

public class IEVDCommand extends CommandHandlerChain {

    @Override
    protected boolean canHandle(String input) {
        return input.matches("^IEVD(\\s|$).*");
    }

    @Override
    public void execute(String input) {
        ConfigManager config = ConfigManager.getInstance();

        if (!validateInput(input)) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Neispravan format naredbe. Očekuje se format 'IEVD dani' (npr. 'IEVD PoSrPeN').");
            return;
        }

        String dani = input.substring(5).trim();

        if (!areValidDays(dani)) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Uneseni dani '" + dani + "' nisu valjani.");
            return;
        }

        TimeTableComposite vozniRed = config.getVozniRed();
        if (vozniRed == null || vozniRed.getChildren().isEmpty()) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Nema dostupnih podataka o voznom redu.");
            return;
        }

        TableBuilder table = new TableBuilder();
        table.setHeaders("Oznaka vlaka", "Oznaka pruge", "Polazišna stanica", "Odredišna stanica", "Vrijeme polaska", "Vrijeme dolaska", "Dani u tjednu");

        Map<String, List<Etapa>> trainEtapasMap = new HashMap<>();

        for (TimeTableComponent trainComponent : vozniRed.getChildren()) {
            if (trainComponent instanceof Train) {
                Train train = (Train) trainComponent;
                List<Etapa> etapas = train.getChildren().stream()
                        .filter(c -> c instanceof Etapa)
                        .map(c -> (Etapa) c)
                        .filter(etapa -> isMatchingDays(etapa.getOznakaDana(), dani))
                        .toList();

                if (!etapas.isEmpty()) {
                    trainEtapasMap.put(train.getOznaka(), etapas);
                }
            }
        }

        List<String> sortedTrainOznake = trainEtapasMap.keySet().stream()
                .sorted(Comparator.comparing(oznaka -> trainEtapasMap.get(oznaka).get(0).getVrijemePolaska()))
                .toList();


        for (String oznakaVlaka : sortedTrainOznake) {
            List<Etapa> etape = trainEtapasMap.get(oznakaVlaka);
            for (Etapa etapa : etape) {
                table.addRow(oznakaVlaka, etapa.getOznakaPruge(), etapa.getPocetnaStanica(),
                        etapa.getOdredisnaStanica(), etapa.getVrijemePolaska(),
                        calculateArrivalTime(etapa.getVrijemePolaska(), etapa.getTrajanjeVoznje()),
                        getDrivingDays(etapa.getOznakaDana()));
            }
        }
        table.build();
    }


    private boolean validateInput(String input) {
        return input.matches("^IEVD\\s+[A-Za-zČčĆćŠšŽž]+$");
    }

    private boolean isMatchingDays(String oznakaDana, String dani) {
        ConfigManager config = ConfigManager.getInstance();

        if (oznakaDana == null) {
            return true;
        }

        Optional<DrivingDays> drivingDaysOpt = config.getDrivingDays().stream()
                .filter(days -> days.getOznaka().equals(oznakaDana))
                .findFirst();

        if (drivingDaysOpt.isPresent()) {
            List<String> availableDays = drivingDaysOpt.get().getDays();

            for (int i = 0; i < dani.length(); ) {
                String dan = (i + 2 <= dani.length()) ? dani.substring(i, i + 2) : dani.substring(i);
                i += dan.length();

                if (!availableDays.contains(dan)) {
                    return false;
                }
            }
            return true;
        }

        return false;
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

    private String getDrivingDays(String oznakaDana) {
        Optional<DrivingDays> drivingDays = ConfigManager.getInstance().getDrivingDays().stream()
                .filter(days -> days.getOznaka().equals(oznakaDana))
                .findFirst();

        return drivingDays.map(days -> String.join(", ", days.getDays()))
                .orElse("Po, U, Sr, Č, Pe, Su, N");
    }

    private boolean areValidDays(String dani) {
        List<String> validDays = List.of("Po", "U", "Sr", "Č", "Pe", "Su", "N");

        int i = 0;
        while (i < dani.length()) {
            boolean found = false;

            if (i + 2 <= dani.length() && validDays.contains(dani.substring(i, i + 2))) {
                i += 2;
                found = true;
            } else if (validDays.contains(dani.substring(i, i + 1))) {
                i += 1;
                found = true;
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }

}
