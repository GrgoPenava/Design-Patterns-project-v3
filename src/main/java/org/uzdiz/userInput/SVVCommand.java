package org.uzdiz.userInput;

import org.uzdiz.ConfigManager;
import org.uzdiz.DrivingDays;
import org.uzdiz.builder.Station;
import org.uzdiz.railwayFactory.Railway;
import org.uzdiz.timeTableComposite.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class SVVCommand extends CommandHandlerChain {

    @Override
    protected boolean canHandle(String input) {
        return input.matches("^SVV(\\s|$).*");
    }

    public List<StationInfo> schedule = new ArrayList<>();
    ConfigManager config = ConfigManager.getInstance();

    static class StationInfo {
        String stationName;
        String time;
        String oznakaPruge;

        public StationInfo(String stationName, String time, String oznakaPruge) {
            this.stationName = stationName;
            this.time = time;
            this.oznakaPruge = oznakaPruge;
        }
    }

    @Override
    public void execute(String input) {
        schedule.clear();

        if (!validateInput(input)) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Neispravan format naredbe. Očekuje se format 'SVV oznaka - dan - koeficijent' (npr. 'SVV 3609 - Po - 60').");
            return;
        }

        String[] parts = input.split("\\s+-\\s+");
        String oznakaVlaka = parts[0].substring(4).trim();
        String dan = parts[1].trim();
        int koeficijent = Integer.parseInt(parts[2].trim());

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

        if (!trainDrivesOnDay(train, dan)) {
            System.out.println("Vlak s oznakom '" + oznakaVlaka + "' ne vozi na dan '" + dan + "'.");
            return;
        }

        processTrainSchedule(train);
        runSimulation(train, koeficijent);
    }

    private boolean validateInput(String input) {
        return input.matches("^SVV\\s+[A-Za-zČčĆćŠšŽž]*\\s*\\d*\\s+-\\s+[A-Za-zČčĆćŠšŽž]+\\s+-\\s+\\d+$");
    }

    private Train findTrainByOznaka(TimeTableComposite vozniRed, String oznakaVlaka) {
        for (TimeTableComponent component : vozniRed.getChildren()) {
            if (component instanceof Train && ((Train) component).getOznaka().equals(oznakaVlaka)) {
                return (Train) component;
            }
        }
        return null;
    }

    private void processTrainSchedule(Train train) {
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
                            schedule.add(new StationInfo(station.getNaziv(), currentTime, station.getOznakaPruge()));
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
                            schedule.add(new StationInfo(station.getNaziv(), currentTime, station.getOznakaPruge()));
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


    private void runSimulation(Train train, int koeficijent) {
        if (schedule.isEmpty()) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Nema stanica za odabrani put.");
            return;
        }

        final boolean[] stopSimulation = {false};
        Set<String> visitedStations = new HashSet<>();

        Thread inputThread = new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                while (!stopSimulation[0]) {
                    if (reader.ready()) {
                        String input = reader.readLine().trim();
                        if (input.equalsIgnoreCase("X")) {
                            stopSimulation[0] = true;
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Greška u unosu");
            }
        });

        inputThread.start();

        System.out.println("Počela je simulacija vožnje vlaka...");

        String virtualTime = schedule.get(0).time;

        for (int i = 0; i < schedule.size(); i++) {
            if (stopSimulation[0]) {
                System.out.println("Simulacija je prekinuta.");
                break;
            }

            StationInfo currentStation = schedule.get(i);

            if (!visitedStations.contains(currentStation.stationName)) {
                System.out.println("Vlak je na stanici " + currentStation.stationName + " (Pruga - " + currentStation.oznakaPruge + ") u " + virtualTime);
                train.notifyObservers("Vlak " + train.getOznaka() + " je na stanici " + currentStation.stationName + " u " + virtualTime);

                StationComposite station = findStationInTrain(train, currentStation.stationName);
                if (station != null) {
                    station.notifyObservers("Vlak " + train.getOznaka() + " je na stanici na koju ste se pretplatili: " + currentStation.stationName + " u " + virtualTime);
                }

                visitedStations.add(currentStation.stationName);
            }

            if (i == schedule.size() - 1) {
                break;
            }

            StationInfo nextStation = schedule.get(i + 1);
            int timeDifference = calculateTimeDifference(currentStation.time, nextStation.time);

            if (timeDifference < 0) {
                config.incrementErrorCount();
                System.out.println("Greška br. " + config.getErrorCount() + ": Negativna razlika vremena između stanica.");
                stopSimulation[0] = true;
                break;
            }

            long sleepTime = timeDifference * 60L * 1000 / koeficijent;

            try {
                for (long remainingTime = sleepTime; remainingTime > 0; remainingTime -= 1000) {
                    if (stopSimulation[0]) {
                        System.out.println("Simulacija je prekinuta u sljedećoj virtualnoj minuti.");
                        return;
                    }
                    Thread.sleep(Math.min(1000, remainingTime));
                }
            } catch (InterruptedException e) {
                System.out.println("Simulacija vožnje vlaka je prekinuta zbog greške.");
                stopSimulation[0] = true;
                break;
            }

            virtualTime = nextStation.time;
        }

        stopSimulation[0] = true;
        inputThread.interrupt();
        System.out.println("Simulacija vožnje vlaka je završena.");
    }


    private int calculateTimeDifference(String startTime, String endTime) {
        String[] startParts = startTime.split(":");
        String[] endParts = endTime.split(":");

        int startHours = Integer.parseInt(startParts[0]);
        int startMinutes = Integer.parseInt(startParts[1]);

        int endHours = Integer.parseInt(endParts[0]);
        int endMinutes = Integer.parseInt(endParts[1]);

        int startTotalMinutes = startHours * 60 + startMinutes;
        int endTotalMinutes = endHours * 60 + endMinutes;

        return endTotalMinutes - startTotalMinutes;
    }

    private StationComposite findStationInTrain(Train train, String stanica) {
        for (TimeTableComponent component : train.getChildren()) {
            if (component instanceof Etapa) {
                Etapa etapa = (Etapa) component;
                for (TimeTableComponent stationComponent : etapa.getChildren()) {
                    if (stationComponent instanceof StationComposite) {
                        StationComposite station = (StationComposite) stationComponent;
                        if (station.getNazivStanice().equals(stanica)) {
                            return station;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean trainDrivesOnDay(Train train, String dan) {
        List<DrivingDays> drivingDaysList = config.getDrivingDays();

        for (TimeTableComponent etapaComponent : train.getChildren()) {
            if (etapaComponent instanceof Etapa) {
                Etapa etapa = (Etapa) etapaComponent;
                String oznakaDana = etapa.getOznakaDana();

                if (oznakaDana == null) {
                    continue;
                }

                Optional<DrivingDays> drivingDaysOpt = drivingDaysList.stream()
                        .filter(days -> days.getOznaka().equals(oznakaDana))
                        .findFirst();

                if (drivingDaysOpt.isEmpty() || !drivingDaysOpt.get().getDays().contains(dan)) {
                    return false;
                }
            }
        }
        return true;
    }
}
