package org.uzdiz.userInputChain;

import org.uzdiz.ConfigManager;
import org.uzdiz.DrivingDays;
import org.uzdiz.builder.Station;
import org.uzdiz.memento.TicketDetails;
import org.uzdiz.railwayFactory.Railway;
import org.uzdiz.stationState.IspravnaState;
import org.uzdiz.stationState.State;
import org.uzdiz.strategy.PriceCalculationStrategy;
import org.uzdiz.strategy.WebMobileStrategy;
import org.uzdiz.strategy.BlagajnaStrategy;
import org.uzdiz.strategy.TrainStrategy;
import org.uzdiz.timeTableComposite.*;
import org.uzdiz.utils.TableBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class UKP2SCommand extends CommandHandlerChain {
    private Map<Station, Integer> stationsList = new LinkedHashMap<>();
    private Map<String, String> stationTimes = new HashMap<>();

    @Override
    protected boolean canHandle(String input) {
        return input.matches("^UKP2S(\\s|$).*");
    }

    @Override
    protected void execute(String input) {
        this.stationsList.clear();
        this.stationTimes.clear();

        if (!arePricesDefined()) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Cijene za vlakove nisu definirane. Koristite komandu CVP za definiranje cijena.");
            return;
        }

        if (!validateInput(input)) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() +
                    ": Neispravan format naredbe. Očekuje se format " +
                    "'UKP2S polaznaStanica - odredišnaStanica - datum - odVr - doVr - načinKupovine'.");
            return;
        }

        String[] parts = input.split("\\s+-\\s+");
        String polaznaStanica = parts[0].substring(6).trim();
        String odredisnaStanica = parts[1].trim();
        String datumStr = parts[2].trim();
        String odVrijeme = parts[3].trim();
        String doVrijeme = parts[4].trim();
        String nacinKupovine = parts[5].trim();

        try {
            LocalTime vrijemeOd2 = parseTime(odVrijeme);
            LocalTime vrijemeDo2 = parseTime(doVrijeme);
        } catch (DateTimeParseException e) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Neispravan format vremena polaska ili dolaska.");
            return;
        }

        LocalDate datum;
        try {
            datum = LocalDate.parse(datumStr, DateTimeFormatter.ofPattern("dd.MM.yyyy."));
        } catch (DateTimeParseException e) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Neispravan format datuma. Očekuje se 'dd.MM.yyyy.'");
            return;
        }
        boolean isWeekend = datum.getDayOfWeek().getValue() >= 6;

        TimeTableComposite vozniRed = ConfigManager.getInstance().getVozniRed();

        if (vozniRed == null || vozniRed.getChildren().isEmpty()) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nema dostupnih podataka o voznom redu.");
            return;
        }

        for (TimeTableComponent component : vozniRed.getChildren()) {
            if (component instanceof Train) {
                String oznakaVlaka = ((Train) component).getOznaka();


                double basePrice = calculateTicketPrice(polaznaStanica, odredisnaStanica, oznakaVlaka);
                if (basePrice == 0) continue;

                switch (nacinKupovine.toUpperCase()) {
                    case "WM":
                        PriceCalculationStrategy webStrategy = new WebMobileStrategy();
                        handleTicketPurchase(webStrategy, oznakaVlaka, polaznaStanica, odredisnaStanica, datum, basePrice, isWeekend, nacinKupovine, odVrijeme, doVrijeme);
                        break;
                    case "B":
                        PriceCalculationStrategy counterStrategy = new BlagajnaStrategy();
                        handleTicketPurchase(counterStrategy, oznakaVlaka, polaznaStanica, odredisnaStanica, datum, basePrice, isWeekend, nacinKupovine, odVrijeme, doVrijeme);
                        break;
                    case "V":
                        PriceCalculationStrategy trainStrategy = new TrainStrategy();
                        handleTicketPurchase(trainStrategy, oznakaVlaka, polaznaStanica, odredisnaStanica, datum, basePrice, isWeekend, nacinKupovine, odVrijeme, doVrijeme);
                        break;
                    default:
                        ConfigManager.getInstance().incrementErrorCount();
                        System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nepoznat način kupovine karte: " + nacinKupovine);
                }
            }
        }
    }

    private void handleTicketPurchase(PriceCalculationStrategy strategy, String oznakaVlaka, String polaznaStanica, String odredisnaStanica, LocalDate datum, double basePrice, boolean isWeekend, String nacinKupovine, String vrijemeOd, String vrijemeDo) {
        Train train = findTrainByOznaka(ConfigManager.getInstance().getVozniRed(), oznakaVlaka);

        if (!isTravelAllowedForAllStages(train, datum)) {
            return;
        }

        double finalPrice = strategy.calculatePrice(basePrice, isWeekend);
        double popustIznos = 0;

        if (finalPrice < basePrice) {
            popustIznos = basePrice - finalPrice;
        }

        try {
            LocalTime vrijemePolaska = parseTime(stationTimes.get("polazak"));
            LocalTime vrijemeDolaska = parseTime(stationTimes.get("dolazak"));
            LocalTime vrijemeOd2 = parseTime(vrijemeOd);
            LocalTime vrijemeDo2 = parseTime(vrijemeDo);

            if (vrijemePolaska.isBefore(vrijemeOd2) || vrijemeDolaska.isAfter(vrijemeDo2)) {
                return;
            }
        } catch (DateTimeParseException e) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Neispravan format vremena polaska ili dolaska.");
            return;
        }

        String vrijemeKupovineKarte = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm"));
        TicketDetails ticketDetails = new TicketDetails(
                oznakaVlaka,
                polaznaStanica,
                odredisnaStanica,
                datum,
                nacinKupovine,
                basePrice,
                stationTimes.get("polazak"),
                stationTimes.get("dolazak"),
                vrijemeKupovineKarte,
                popustIznos,
                finalPrice
        );

        TableBuilder table = new TableBuilder();
        table.setHeaders(
                "Vlak",
                "Polazna stanica",
                "Odredišna stanica",
                "Datum",
                "Način kupovine",
                "Vrijeme polaska",
                "Vrijeme dolaska",
                "Izvorna cijena",
                "Popust",
                "Konačna cijena"
        );

        table.addRow(
                ticketDetails.getTicketOznakaVlaka(),
                ticketDetails.getPolaznaStanica(),
                ticketDetails.getOdredisnaStanica(),
                ticketDetails.getDatum().toString(),
                ticketDetails.getNacinKupovine(),
                ticketDetails.getVrijemePolaska(),
                ticketDetails.getVrijemeDolaska(),
                String.format("%.2f", ticketDetails.getIzvornaCijena()),
                String.format("%.2f", ticketDetails.getPopustiIznos()),
                String.format("%.2f", ticketDetails.getKonacnaCijena())
        );

        table.build();
        System.out.println();
        System.out.println();
    }

    private boolean validateInput(String input) {
        String regex = "^UKP2S\\s+.+?\\s+-\\s+.+?\\s+-\\s+\\d{2}\\.\\d{2}\\.\\d{4}\\.\\s+-\\s+\\d{1,2}:\\d{2}\\s+-\\s+\\d{1,2}:\\d{2}\\s+-\\s+\\S+$";
        return input.matches(regex);
    }

    private double calculateTicketPrice(String polaznaStanica, String odredisnaStanica, String oznakaVlaka) {
        ConfigManager config = ConfigManager.getInstance();
        TimeTableComposite vozniRed = config.getVozniRed();

        if (vozniRed == null || vozniRed.getChildren().isEmpty()) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Nema dostupnih podataka o voznom redu.");
            return 0;
        }

        Train train = findTrainByOznaka(vozniRed, oznakaVlaka);

        if (train == null) {
            return 0;
        }

        if (!processTrainSchedule(train, polaznaStanica, odredisnaStanica)) return 0;

        String vrstaVlaka = train.getVrstaVlaka();
        double cijenaPoKilometru = switch (vrstaVlaka) {
            case "N" -> config.getTicketPrice().cijenaNormalni;
            case "U" -> config.getTicketPrice().cijenaUbrzani;
            case "B" -> config.getTicketPrice().cijenaBrzi;
            default -> 0;
        };

        return cijenaPoKilometru * getLastDistanceFromStationsList();
    }

    private boolean processTrainSchedule(Train train, String polaznaStanica, String odredisnaStanica) {
        int totalDistance = 0;
        boolean withinRange = false;
        boolean smjerValidan = false;

        if (!checkTrainDirection(train, polaznaStanica, odredisnaStanica)) {
            return false;
        }

        for (TimeTableComponent etapaComponent : train.getChildren()) {
            if (etapaComponent instanceof Etapa) {
                Etapa etapa = (Etapa) etapaComponent;
                String oznakaPruge = etapa.getOznakaPruge();
                String currentTime = etapa.getVrijemePolaska();

                Railway railway = ConfigManager.getInstance().getRailwayByOznakaPruge(oznakaPruge);
                if (railway == null) {
                    continue;
                }

                List<Station> stations = railway.getPopisSvihStanica();
                int startIndex = findStationIndex(stations, etapa.getPocetnaStanica());
                int endIndex = findStationIndex(stations, etapa.getOdredisnaStanica());

                if (startIndex == -1 || endIndex == -1) {
                    continue;
                }
                boolean isNormalDirection = startIndex < endIndex;

                String vrstaVlaka = train.getVrstaVlaka();

                if ("O".equals(etapa.getSmjer())) {
                    if (findStationIndex(stations, polaznaStanica) >= findStationIndex(stations, odredisnaStanica)) {
                        smjerValidan = true;
                    }

                    for (int i = startIndex; i >= endIndex; i--) {
                        Station station = stations.get(i);

                        if (station.getNaziv().equals(polaznaStanica)) {
                            withinRange = true;
                            totalDistance = 0;
                            stationTimes.put("polazak", currentTime);
                        }

                        if (withinRange) {
                            this.stationsList.put(station, totalDistance);
                        }

                        if (station.getNaziv().equals(odredisnaStanica)) {
                            withinRange = false;
                            stationTimes.put("dolazak", currentTime);
                            break;
                        }

                        if (i > endIndex) {
                            int duzinaDoPrethodneStanice = stations.get(i).getDuzina();
                            int vrijemeZaustavljanja = getVrijemeZaustavljanja(station, vrstaVlaka);

                            totalDistance += duzinaDoPrethodneStanice;
                            currentTime = calculateNewTime(currentTime, vrijemeZaustavljanja);
                        }
                    }
                } else {
                    if (findStationIndex(stations, polaznaStanica) <= findStationIndex(stations, odredisnaStanica)) {
                        smjerValidan = true;
                    }

                    for (int i = startIndex; i <= endIndex; i++) {
                        Station station = stations.get(i);

                        if (station.getNaziv().equals(polaznaStanica)) {
                            withinRange = true;
                            totalDistance = 0;
                            stationTimes.put("polazak", currentTime);
                        }

                        if (withinRange) {
                            this.stationsList.put(station, totalDistance);
                        }

                        if (station.getNaziv().equals(odredisnaStanica)) {
                            withinRange = false;
                            stationTimes.put("dolazak", currentTime);
                            break;
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

        if (!areStationsOperational(train.getOznaka(), polaznaStanica, odredisnaStanica)) {
            return false;
        }

        if (!smjerValidan) {
            return false;
        }
        return true;

    }

    private int getLastDistanceFromStationsList() {
        return new ArrayList<>(stationsList.values()).get(stationsList.size() - 1);
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

    private Train findTrainByOznaka(TimeTableComposite vozniRed, String oznakaVlaka) {
        for (TimeTableComponent component : vozniRed.getChildren()) {
            if (component instanceof Train && ((Train) component).getOznaka().equals(oznakaVlaka)) {
                return (Train) component;
            }
        }
        return null;
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

    private boolean checkTrainDirection(Train train, String polaznaStanica, String odredisnaStanica) {
        List<String> stationNames = new ArrayList<>();

        for (TimeTableComponent etapaComponent : train.getChildren()) {
            if (etapaComponent instanceof Etapa) {
                Etapa etapa = (Etapa) etapaComponent;

                for (TimeTableComponent stationComponent : etapa.getChildren()) {
                    if (stationComponent instanceof StationComposite) {
                        StationComposite station = (StationComposite) stationComponent;
                        stationNames.add(station.getNazivStanice());
                    }
                }
            }
        }

        int indexPolazna = stationNames.indexOf(polaznaStanica);
        int indexOdredisna = stationNames.indexOf(odredisnaStanica);

        if (indexPolazna == -1 || indexOdredisna == -1) {
            return false;
        }

        if (indexOdredisna < indexPolazna) {
            return false;
        }

        return true;
    }

    private boolean isTravelAllowedForAllStages(Train train, LocalDate datum) {
        List<DrivingDays> drivingDaysList = ConfigManager.getInstance().getDrivingDaysList();

        String danUTjednu = switch (datum.getDayOfWeek()) {
            case MONDAY -> "Po";
            case TUESDAY -> "U";
            case WEDNESDAY -> "Sr";
            case THURSDAY -> "Č";
            case FRIDAY -> "Pe";
            case SATURDAY -> "Su";
            case SUNDAY -> "N";
        };

        for (TimeTableComponent etapaComponent : train.getChildren()) {
            if (etapaComponent instanceof Etapa etapa) {
                DrivingDays matchingDrivingDay = drivingDaysList.stream()
                        .filter(drivingDay -> drivingDay.getOznaka().equals(etapa.getOznakaDana()))
                        .findFirst()
                        .orElse(null);

                if (etapa.getOznakaDana() != null && (matchingDrivingDay == null || !matchingDrivingDay.getDays().contains(danUTjednu))) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean arePricesDefined() {
        ConfigManager configManager = ConfigManager.getInstance();
        return configManager.getTicketPrice() != null;
    }

    private boolean areStationsOperational(String oznakaVlaka, String polaznaStanica, String odredisnaStanica) {
        Train train = findTrainByOznaka(ConfigManager.getInstance().getVozniRed(), oznakaVlaka);

        if (train == null) {
            return false;
        }

        for (TimeTableComponent etapaComponent : train.getChildren()) {
            if (etapaComponent instanceof Etapa etapa) {
                List<StationComposite> stations = getStationComposites(etapa);

                boolean isReverseDirection = etapa.getSmjer().equals("O");
                boolean withinRange = false;

                for (int i = 0; i < stations.size(); i++) {
                    StationComposite station = stations.get(i);

                    if (station.getNazivStanice().equals(polaznaStanica)) {
                        withinRange = true;
                    }

                    if (withinRange) {
                        State stateToCheck;
                        if (station.getBrojKolosjeka() == 1) {
                            stateToCheck = station.getState(0);
                        } else {
                            stateToCheck = isReverseDirection ? station.getState(1) : station.getState(0);
                        }

                        if (!(stateToCheck instanceof IspravnaState)) {
                            return false;
                        }
                    }

                    if (station.getNazivStanice().equals(odredisnaStanica)) {
                        withinRange = false;
                    }
                }
            }
        }
        return true;
    }

    private List<StationComposite> getStationComposites(Etapa etapa) {
        List<StationComposite> stationComposites = new ArrayList<>();
        for (TimeTableComponent component : etapa.getChildren()) {
            if (component instanceof StationComposite) {
                stationComposites.add((StationComposite) component);
            }
        }
        return stationComposites;
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