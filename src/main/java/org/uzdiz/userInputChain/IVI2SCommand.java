package org.uzdiz.userInputChain;

import org.uzdiz.ConfigManager;
import org.uzdiz.builder.Station;
import org.uzdiz.railwayFactory.Railway;
import org.uzdiz.timeTableComposite.*;
import org.uzdiz.utils.TableBuilder;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class IVI2SCommand extends CommandHandlerChain {

    @Override
    protected boolean canHandle(String input) {
        return input.matches("^IVI2S(\\s|$).*");
    }

    @Override
    protected void execute(String input) {
        String[] parts = input.split("\\s+-\\s+");
        if (parts.length != 6) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() +
                    ": Neispravan format naredbe. Očekčuje se format 'IVI2S polaznaStanica - odredišnaStanica - dan - odVr - doVr - prikaz'.");
            return;
        }

        String polaznaStanica = parts[0].substring(6).trim();
        String odredisnaStanica = parts[1].trim();
        String danUTjednu = parts[2].trim();
        String odVrijemeStr = parts[3].trim();
        String doVrijemeStr = parts[4].trim();
        String prikaz = parts[5].trim();

        LocalTime vrijemeOd, vrijemeDo;
        try {
            vrijemeOd = parseTime(odVrijemeStr);
            vrijemeDo = parseTime(doVrijemeStr);
        } catch (DateTimeParseException e) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() +
                    ": Neispravan format vremena polaska ili dolaska.");
            return;
        }

        TimeTableComposite vozniRed = ConfigManager.getInstance().getVozniRed();
        if (vozniRed == null || vozniRed.getChildren().isEmpty()) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nema dostupnih podataka o voznom redu.");
            return;
        }

        TableBuilder table = new TableBuilder();
        table.setHeaders("Oznaka vlaka", "Oznaka pruge", "Željeznička stanica", "Vrijeme polaska", "Broj km od polazne stanice");

        for (TimeTableComponent component : vozniRed.getChildren()) {
            if (component instanceof Train train) {
                processTrainSchedule(train, table, polaznaStanica, odredisnaStanica, vrijemeOd, vrijemeDo);
            }
        }
        table.build();
    }

    private void processTrainSchedule(Train train, TableBuilder table, String polaznaStanica, String odredisnaStanica, LocalTime vrijemeOd, LocalTime vrijemeDo) {
        boolean withinRange = false;
        boolean isTrainProcessed = false;
        int totalDistance = 0;
        String currentTime = "00:00";

        for (TimeTableComponent etapaComponent : train.getChildren()) {
            if (etapaComponent instanceof Etapa etapa) {
                String oznakaPruge = etapa.getOznakaPruge();
                currentTime = etapa.getVrijemePolaska();

                Railway railway = ConfigManager.getInstance().getRailwayByOznakaPruge(oznakaPruge);
                if (railway == null) {
                    ConfigManager.getInstance().incrementErrorCount();
                    System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Pruga s oznakom '" + oznakaPruge + "' nije pronađena.");
                    continue;
                }

                List<Station> stations = railway.getPopisSvihStanica();
                int startIndex = findStationIndex(stations, etapa.getPocetnaStanica());
                int endIndex = findStationIndex(stations, etapa.getOdredisnaStanica());

                if (startIndex == -1 || endIndex == -1) {
                    ConfigManager.getInstance().incrementErrorCount();
                    System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Stanice etape nisu pronađene na pruzi '" + oznakaPruge + "'.");
                    continue;
                }

                for (int i = startIndex; i <= endIndex; i++) {
                    Station station = stations.get(i);

                    if (station.getNaziv().equals(polaznaStanica)) {
                        withinRange = true;
                        totalDistance = 0;
                        currentTime = etapa.getVrijemePolaska();
                    }

                    if (withinRange) {
                        LocalTime polazakTime = parseTime(currentTime);
                        if (polazakTime.isBefore(vrijemeOd) || polazakTime.isAfter(vrijemeDo)) {
                            break;
                        }

                        table.addRow(train.getOznaka(), oznakaPruge, station.getNaziv(), currentTime, String.valueOf(totalDistance));
                        isTrainProcessed = true;

                        if (station.getNaziv().equals(odredisnaStanica)) {
                            withinRange = false;
                            break;
                        }
                    }

                    if (i < endIndex) {
                        totalDistance += stations.get(i + 1).getDuzina();
                        currentTime = calculateNewTime(currentTime, getVrijemeZaustavljanja(stations.get(i + 1), train.getVrstaVlaka()));
                    }
                }
            }
        }

        if (isTrainProcessed) {
            table.addEmptyRow();
        }
    }

    private int getVrijemeZaustavljanja(Station station, String vrstaVlaka) {
        return switch (vrstaVlaka) {
            case "U" -> station.getVrijemeUbrzaniVlak() != null ? station.getVrijemeUbrzaniVlak() : 0;
            case "B" -> station.getVrijemeBrziVlak() != null ? station.getVrijemeBrziVlak() : 0;
            default -> station.getVrijemeNormalniVlak() != null ? station.getVrijemeNormalniVlak() : 0;
        };
    }

    private int findStationIndex(List<Station> stations, String stationName) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.get(i).getNaziv().equals(stationName)) {
                return i;
            }
        }
        return -1;
    }

    private String calculateNewTime(String currentTime, int dodatneMinute) {
        try {
            String[] parts = currentTime.split(":");
            int sati = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            minute += dodatneMinute;
            sati += minute / 60;
            minute %= 60;
            sati %= 24;

            return String.format("%02d:%02d", sati, minute);
        } catch (Exception e) {
            return "//";
        }
    }

    private LocalTime parseTime(String timeStr) {
        try {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e1) {
            try {
                return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("H:mm"));
            } catch (DateTimeParseException e2) {
                throw new DateTimeParseException("Neispravan format vremena: " + timeStr, timeStr, 0);
            }
        }
    }
}
