package org.uzdiz.userInput;

import org.uzdiz.ConfigManager;
import org.uzdiz.builder.Station;
import org.uzdiz.railwayFactory.Railway;
import org.uzdiz.timeTableComposite.Etapa;
import org.uzdiz.timeTableComposite.TimeTableComponent;
import org.uzdiz.timeTableComposite.TimeTableComposite;
import org.uzdiz.timeTableComposite.Train;
import org.uzdiz.utils.TableBuilder;

import java.util.List;

public class IVRVCommand implements Command {
    @Override
    public void execute(String input) {

        ConfigManager config = ConfigManager.getInstance();

        if (!validateInput(input)) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Neispravan format naredbe. Očekuje se format 'IVRV oznaka' (npr. 'IVRV 3609').");
            return;
        }

        String oznakaVlaka = input.substring(5).trim();

        TimeTableComposite vozniRed = config.getVozniRed();

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
        table.setHeaders("Oznaka vlaka", "Oznaka pruge", "Željeznička stanica", "Vrijeme polaska", "Broj km od polazne stanice");

        processTrainSchedule(train, table);

        table.build();
    }

    private boolean validateInput(String input) {
        return input.matches("^IVRV\\s+[A-Za-zČčĆćŠšŽž]*\\s*\\d+$");
    }

    private Train findTrainByOznaka(TimeTableComposite vozniRed, String oznakaVlaka) {
        for (TimeTableComponent component : vozniRed.getChildren()) {
            if (component instanceof Train && ((Train) component).getOznaka().equals(oznakaVlaka)) {
                return (Train) component;
            }
        }
        return null;
    }

    private void processTrainSchedule(Train train, TableBuilder table) {
        String oznakaVlaka = train.getOznaka();
        int totalDistance = 0;

        String currentTime = "00:00";

        for (TimeTableComponent etapaComponent : train.getChildren()) {
            if (etapaComponent instanceof Etapa) {
                Etapa etapa = (Etapa) etapaComponent;
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

                String vrstaVlaka = train.getVrstaVlaka();

                if ("O".equals(etapa.getSmjer())) {
                    for (int i = startIndex; i >= endIndex; i--) {
                        Station station = stations.get(i);

                        if (i == startIndex || i == endIndex || getVrijemeZaustavljanja(station, vrstaVlaka) > 0) {
                            table.addRow(oznakaVlaka, oznakaPruge, station.getNaziv(), currentTime, String.valueOf(totalDistance));
                        }

                        if (i > endIndex) {
                            int duzinaDoPrethodneStanice = stations.get(i).getDuzina();
                            int vrijemeZaustavljanja = getVrijemeZaustavljanja(station, vrstaVlaka);

                            totalDistance += duzinaDoPrethodneStanice;
                            currentTime = calculateNewTime(currentTime, vrijemeZaustavljanja);
                        }
                    }
                } else {
                    for (int i = startIndex; i <= endIndex; i++) {
                        Station station = stations.get(i);

                        if (i == startIndex || i == endIndex || getVrijemeZaustavljanja(station, vrstaVlaka) > 0) {
                            table.addRow(oznakaVlaka, oznakaPruge, station.getNaziv(), currentTime, String.valueOf(totalDistance));
                        }

                        if (i < endIndex) {
                            totalDistance += stations.get(i + 1).getDuzina();
                            int dodatneMinute = getVrijemeZaustavljanja(stations.get(i + 1), vrstaVlaka);

                            if (dodatneMinute == 0) {
                                dodatneMinute = findNextStationTimeWithSameName(stations.get(i + 1), vrstaVlaka);
                            }

                            currentTime = calculateNewTime(currentTime, dodatneMinute);

                        }
                    }
                }
            }
        }
    }


    private int findNextStationTimeWithSameName(Station currentStation, String vrstaVlaka) {
        List<Station> allStations = ConfigManager.getInstance().getStations();
        boolean found = false;

        for (Station station : allStations) {
            if (found && station.getNaziv().equals(currentStation.getNaziv())) {
                return getVrijemeZaustavljanja(station, vrstaVlaka);
            }
            if (station.getId().equals(currentStation.getId())) {
                found = true;
            }
        }

        return 0;
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


}
