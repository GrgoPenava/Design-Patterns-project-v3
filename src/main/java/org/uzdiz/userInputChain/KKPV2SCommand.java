package org.uzdiz.userInputChain;

import org.uzdiz.ConfigManager;
import org.uzdiz.DrivingDays;
import org.uzdiz.builder.Station;
import org.uzdiz.memento.TicketMemento;
import org.uzdiz.memento.TicketOriginator;
import org.uzdiz.memento.TicketPurchase;
import org.uzdiz.railwayFactory.Railway;
import org.uzdiz.stationState.IspravnaState;
import org.uzdiz.stationState.State;
import org.uzdiz.strategy.BlagajnaStrategy;
import org.uzdiz.strategy.PriceCalculationStrategy;
import org.uzdiz.strategy.TrainStrategy;
import org.uzdiz.strategy.WebMobileStrategy;
import org.uzdiz.timeTableComposite.*;
import org.uzdiz.utils.TableBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class KKPV2SCommand extends CommandHandlerChain{
    private Map<Station, Integer> stationsList = new LinkedHashMap<>();
    private Map<String, String> stationTimes = new HashMap<>();
    private Map<StationComposite, Integer> stationsList2 = new LinkedHashMap<>();

    @Override
    protected boolean canHandle(String input) {
        return input.matches("^KKPV2S(\\s|$).*");
    }

    @Override
    protected void execute(String input) {
        this.stationsList.clear();
        this.stationTimes.clear();
        this.stationsList2.clear();

        if (!arePricesDefined()) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Cijene za vlakove nisu definirane. Koristite komandu CVP za definiranje cijena.");
            return;
        }

        if (!validateInput(input)) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Neispravan format naredbe. Očekuje se format 'KKPV2S oznaka - polaznaStanica - odredišnaStanica - datum - načinKupovine'.");
            return;
        }

        String regex = "^KKPV2S\\s+(\\S+)\\s+-\\s+(.+?)\\s+-\\s+(.+?)\\s+-\\s+(\\d{2}\\.\\d{2}\\.\\d{4}\\.)\\s+-\\s+(\\S+)$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(input);

        if (!matcher.matches()) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Neispravan format naredbe.");
            return;
        }

        String oznakaVlaka = matcher.group(1);
        String polaznaStanica = matcher.group(2).trim();
        String odredisnaStanica = matcher.group(3).trim();
        String datumStr = matcher.group(4);
        String nacinKupovine = matcher.group(5);

        LocalDate datum;
        try {
            datum = LocalDate.parse(datumStr, DateTimeFormatter.ofPattern("dd.MM.yyyy."));
        } catch (DateTimeParseException e) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Neispravan format datuma. Očekuje se 'dd.MM.yyyy.'");
            return;
        }

        double basePrice = calculateTicketPrice(polaznaStanica, odredisnaStanica, oznakaVlaka);
        if (basePrice == 0) return;
        boolean isWeekend = datum.getDayOfWeek().getValue() >= 6;

        switch (nacinKupovine.toUpperCase()) {
            case "WM":
                PriceCalculationStrategy webStrategy = new WebMobileStrategy();
                handleTicketPurchase(webStrategy, oznakaVlaka, polaznaStanica, odredisnaStanica, datum, basePrice, isWeekend, nacinKupovine);
                break;
            case "B":
                PriceCalculationStrategy counterStrategy = new BlagajnaStrategy();
                handleTicketPurchase(counterStrategy, oznakaVlaka, polaznaStanica, odredisnaStanica, datum, basePrice, isWeekend, nacinKupovine);
                break;
            case "V":
                PriceCalculationStrategy trainStrategy = new TrainStrategy();
                handleTicketPurchase(trainStrategy, oznakaVlaka, polaznaStanica, odredisnaStanica, datum, basePrice, isWeekend, nacinKupovine);
                break;
            default:
                ConfigManager.getInstance().incrementErrorCount();
                System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nepoznat način kupovine karte: " + nacinKupovine);
        }
    }

    private void handleTicketPurchase(PriceCalculationStrategy strategy, String oznakaVlaka, String polaznaStanica, String odredisnaStanica, LocalDate datum, double basePrice, boolean isWeekend, String nacinKupovine) {
        Train train = findTrainByOznaka(ConfigManager.getInstance().getVozniRed(), oznakaVlaka);

        if (!isTravelAllowedForAllStages(train, datum)) {
            return;
        }

        TicketOriginator originator = new TicketOriginator();
        TicketPurchase purchase = new TicketPurchase(strategy, originator);

        purchase.purchaseTicket(oznakaVlaka, polaznaStanica, odredisnaStanica, datum, basePrice, isWeekend, nacinKupovine, stationTimes.get("polazak"), stationTimes.get("dolazak"));
        TicketMemento memento = ConfigManager.getInstance().getTicketCareTaker().getLastMemento();

        if (memento != null) {
            TableBuilder table = new TableBuilder();
            table.setHeaders("Redni broj", "Oznaka vlaka", "Polazna stanica", "Odredišna stanica", "Datum", "Način kupovine", "Izvorna cijena", "Popust", "Konačna cijena", "Vrijeme polaska", "Vrijeme dolaska", "Vrijeme kupovine");
            table.addRow(
                    "1", // Redni broj je uvijek 1 jer ispisujemo samo ovu kartu
                    memento.getTicketDetails().getTicketOznakaVlaka(),
                    memento.getTicketDetails().getPolaznaStanica(),
                    memento.getTicketDetails().getOdredisnaStanica(),
                    memento.getTicketDetails().getDatum().toString(),
                    memento.getTicketDetails().getNacinKupovine(),
                    String.format("%.2f", memento.getTicketDetails().getIzvornaCijena()),
                    String.format("%.2f", memento.getTicketDetails().getPopustiIznos()),
                    String.format("%.2f", memento.getTicketDetails().getKonacnaCijena()),
                    memento.getTicketDetails().getVrijemePolaska(),
                    memento.getTicketDetails().getVrijemeDolaska(),
                    memento.getTicketDetails().getVrijemeKupovineKarte()
            );
            table.build();
        }
    }

    private boolean validateInput(String input) {
        String regex = "^KKPV2S\\s+\\S+\\s+-\\s+.+?\\s+-\\s+.+?\\s+-\\s+\\d{2}\\.\\d{2}\\.\\d{4}\\.\\s+-\\s+\\S+$";
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
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Vlak s oznakom '" + oznakaVlaka + "' nije pronađen.");
            return 0;
        }

        if (!processTrainSchedule2(train, polaznaStanica, odredisnaStanica)) return 0;

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
            return false; // Prekida daljnje izvršavanje ako smjer nije valjan
        }

        for (TimeTableComponent etapaComponent : train.getChildren()) {
            if (etapaComponent instanceof Etapa) {
                Etapa etapa = (Etapa) etapaComponent;
                String oznakaPruge = etapa.getOznakaPruge();
                String currentTime = etapa.getVrijemePolaska();

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
                            System.out.println("SSa -" + station.getNaziv());
                        }

                        if (withinRange) {
                            System.out.println("SSb -" + station.getNaziv());
                            //TODO provjera stanja

                            /*if (station.getBrojKolosjeka() == 2) {
                                if (!(station.getStateObrnuti() instanceof IspravnaState)) {
                                    System.out.println("Stanica " + station.getNaziv() + " je u stanju " + station.getStateObrnuti().getStatusName() + ". Nije moguće kupiti kartu.");
                                    return false;
                                }
                            } else if (station.getBrojKolosjeka() == 1) {
                                if (!(station.getStateNormalni() instanceof IspravnaState)) {
                                    System.out.println("Stanica " + station.getNaziv() + " je u stanju " + station.getStateNormalni().getStatusName() + ". Nije moguće kupiti kartu.");
                                    return false;
                                }
                            }*/

                            this.stationsList.put(station, totalDistance);
                        }


                        if (station.getNaziv().equals(odredisnaStanica)) {
                            withinRange = false;
                            stationTimes.put("dolazak", currentTime);
                            System.out.println("SSc -" + station.getNaziv());
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
                            System.out.println("SS2 -" + station.getNaziv());
                            stationTimes.put("polazak", currentTime);
                        }

                        if (withinRange) {
                            System.out.println("SS2 -" + station.getNaziv());

                            /*if (station.getBrojKolosjeka() == 1) {
                                if (!(station.getStateNormalni() instanceof IspravnaState)) {
                                    System.out.println("Stanica " + station.getNaziv() + " je u stanju " + station.getStateNormalni().getStatusName() + ". Nije moguće kupiti kartu.");
                                    return false;
                                }
                            }*/

                            this.stationsList.put(station, totalDistance);
                        }


                        if (station.getNaziv().equals(odredisnaStanica)) {
                            withinRange = false;
                            System.out.println("SS2 -" + station.getNaziv());
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
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Vlak ne ide u smjeru između stanica " + polaznaStanica + " i " + odredisnaStanica + ".");
            return false;
        }
        return true;

    }

    private boolean processTrainSchedule2(Train train, String polaznaStanica, String odredisnaStanica) {
        int totalDistance = 0;
        boolean withinRange = false;
        StationComposite previousStation = null;

        for (TimeTableComponent etapaComponent : train.getChildren()) {
            if (etapaComponent instanceof Etapa etapa) {
                List<StationComposite> stations = getStationComposites(etapa);

                for (StationComposite currentStation : stations) {
                    if (currentStation.getNazivStanice().equals(polaznaStanica)) {
                        withinRange = true;
                        totalDistance = 0; // Resetiramo udaljenost
                        stationTimes.put("polazak", etapa.getVrijemePolaska());
                    }

                    if (withinRange) {
                        if (previousStation != null) {
                            State previousState = previousStation.getState(0);
                            State currentState = currentStation.getState(0);

                            if (previousState != null && currentState != null) {
                                if (!(previousState instanceof IspravnaState) &&
                                        !(currentState instanceof IspravnaState) &&
                                        previousState.getStatus().equalsIgnoreCase(currentState.getStatus())) {
                                    System.out.println("Relacija '" + previousStation.getNazivStanice() + " - " +
                                            currentStation.getNazivStanice() + "' je u stanju '" + currentState.getStatus() +
                                            "'. Nije moguće kupiti kartu.");
                                    return false;
                                }
                            }
                        }

                        previousStation = currentStation;
                        stationsList2.put(currentStation, totalDistance);
                    }

                    if (currentStation.getNazivStanice().equals(odredisnaStanica)) {
                        withinRange = false;
                        stationTimes.put("dolazak", etapa.getVrijemePolaska());
                        break;
                    }
                }
            }
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
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Polazna ili odredišna stanica nije pronađena u vlaku.");
            return false; // Prekida ako stanice nisu pronađene
        }

        if (indexOdredisna < indexPolazna) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Vlak ne ide u tom smjeru između stanica " + polaznaStanica + " i " + odredisnaStanica + ".");
            return false; // Prekida ako smjer nije ispravan
        }

        return true; // Smjer je ispravan
    }

    private boolean isTravelAllowedForAllStages(Train train, LocalDate datum) {
        List<DrivingDays> drivingDaysList = ConfigManager.getInstance().getDrivingDaysList();

        // Pretvori datum u odgovarajući dan u tjednu
        String danUTjednu = switch (datum.getDayOfWeek()) {
            case MONDAY -> "Po";
            case TUESDAY -> "U";
            case WEDNESDAY -> "Sr";
            case THURSDAY -> "Č";
            case FRIDAY -> "Pe";
            case SATURDAY -> "Su";
            case SUNDAY -> "N";
        };

        // Prođi kroz sve etape vlaka
        for (TimeTableComponent etapaComponent : train.getChildren()) {
            if (etapaComponent instanceof Etapa etapa) {
                // Pronađi DrivingDays koji odgovara oznakaDana
                DrivingDays matchingDrivingDay = drivingDaysList.stream()
                        .filter(drivingDay -> drivingDay.getOznaka().equals(etapa.getOznakaDana()))
                        .findFirst()
                        .orElse(null);

                if (etapa.getOznakaDana() != null && (matchingDrivingDay == null || !matchingDrivingDay.getDays().contains(danUTjednu))) {
                    System.out.println("Greška: Vlak '" + train.getOznaka() +
                            "' ne putuje na odabran dan");
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
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Vlak s oznakom '" + oznakaVlaka + "' nije pronađen.");
            return false;
        }

        for (TimeTableComponent etapaComponent : train.getChildren()) {
            if (etapaComponent instanceof Etapa etapa) {
                List<StationComposite> stations = getStationComposites(etapa);

                boolean isReverseDirection = etapa.getSmjer().equals("O");
                boolean withinRange = false;

                for (int i = 0; i < stations.size(); i++) {
                    StationComposite station = stations.get(i);

                    // Provjera unutar dosega polazne i odredišne stanice
                    if (station.getNazivStanice().equals(polaznaStanica)) {
                        withinRange = true;
                    }

                    if (withinRange) {
                        // Provjeravamo stanje kolosijeka
                        State stateToCheck;
                        if (station.getBrojKolosjeka() == 1) {
                            stateToCheck = station.getState(0); // Jedini kolosijek
                        } else {
                            stateToCheck = isReverseDirection ? station.getState(1) : station.getState(0); // Obrnuti ili normalni smjer
                        }

                        // Ako stanje nije IspravnaState, zaustavljamo kupovinu
                        if (!(stateToCheck instanceof IspravnaState)) {
                            System.out.println("Stanica '" + station.getNazivStanice() + "' je u stanju '" + stateToCheck.getStatus() + "'. Nije moguće kupiti kartu.");
                            return false;
                        }
                    }

                    if (station.getNazivStanice().equals(odredisnaStanica)) {
                        withinRange = false;
                    }
                }
            }
        }
        return true; // Sve stanice su operativne
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
}