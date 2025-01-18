package org.uzdiz.readerFactory;

import org.uzdiz.builder.Station;
import org.uzdiz.builder.TimeTable;
import org.uzdiz.ConfigManager;
import org.uzdiz.railwayFactory.Railway;
import org.uzdiz.timeTableComposite.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeTableReaderProduct implements CsvReaderProduct {
    private List<TimeTable> timeTables = new ArrayList<>();
    private String path;
    private Integer fileErrorCounter = 0;

    TimeTableComposite vozniRed = ConfigManager.getInstance().getVozniRed();

    private Map<String, Train> trainMap = new HashMap<>();

    @Override
    public void loadData(String filePath) {
        this.path = filePath;

        if (vozniRed == null) {
            vozniRed = new VozniRed("Vozni red");
            ConfigManager.getInstance().setVozniRed(vozniRed);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                if (line.startsWith("#")) continue;

                String[] data = line.split(";", -1);

                if (!validateData(data)) {
                    continue;
                }

                String formattedTime = formatTime(data[6]);

                TimeTable.TimeTableBuilder builder = new TimeTable.TimeTableBuilder(data[0], data[1], data[4], formattedTime);

                if (!data[2].isEmpty()) {
                    builder.setPolaznaStanica(data[2]);
                }

                if (!data[3].isEmpty()) {
                    builder.setOdredisnaStanica(data[3]);
                }

                String vrstaVlaka = switch (data[5]) {
                    case "U" -> "U";
                    case "B" -> "B";
                    default -> "N";
                };
                builder.setVrstaVlaka(vrstaVlaka);

                if (!data[7].isEmpty()) {
                    builder.setTrajanjeVoznje(data[7]);
                }

                if (!data[8].isEmpty()) {
                    builder.setOznakaDana(data[8]);
                }

                timeTables.add(builder.build());
            }

            removeInvalidTrains();

            ConfigManager.getInstance().setTimeTables(timeTables);
            createVozniRed(timeTables);
            validateAndRemoveInvalidTrains(vozniRed);
        } catch (IOException e) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nije moguće učitati datoteku - " + filePath);
            this.printFileError();
        }
    }

    private void printFileError() {
        this.fileErrorCounter++;
        System.out.println("-> Greška datoteke (" + path + ") br. " + this.fileErrorCounter);
    }

    private boolean validateData(String[] data) {
        boolean isValid = true;

        if (isEmptyRow(data)) {
            return false;
        }

        if (data.length != 9) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Zapis nije potpun, očekuje se 9 podataka, ali postoji " + data.length);
            return false;
        }

        if (data[0].isEmpty()) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nedostaje vrijednost za 'Oznaka pruge'.");
            isValid = false;
        }

        if (data[1].isEmpty()) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nedostaje vrijednost za 'Smjer'.");
            isValid = false;
        } else if (!data[1].equals("N") && !data[1].equals("O")) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Neispravna vrijednost za 'Smjer'. Dozvoljene vrijednosti su 'N' ili 'O'.");
            isValid = false;
        }

        if (data[4].isEmpty()) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nedostaje vrijednost za 'Oznaka vlaka'.");
            isValid = false;
        }

        if (data[6].isEmpty()) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nedostaje vrijednost za 'Vrijeme polaska'.");
            isValid = false;
        }

        if (!data[2].isEmpty() && !isStationValid(data[2])) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Polazna stanica '" + data[2] + "' ne postoji u popisu svih stanica.");
            isValid = false;
        }

        if (!data[3].isEmpty() && !isStationValid(data[3])) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Odredišna stanica '" + data[3] + "' ne postoji u popisu svih stanica.");
            isValid = false;
        }

        if (!isValid) {
            this.printFileError();
        }

        return isValid;
    }

    private boolean isEmptyRow(String[] data) {
        for (String field : data) {
            if (field != null && !field.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean isStationValid(String stationName) {
        return ConfigManager.getInstance().getStations().stream()
                .anyMatch(station -> station.getNaziv().equals(stationName));
    }

    private void createVozniRed(List<TimeTable> timeTables) {
        for (TimeTable timeTable : timeTables) {
            String oznakaPruge = timeTable.getOznakaPruge();
            String oznakaVlaka = timeTable.getOznakaVlaka();
            String vrstaVlaka = timeTable.getVrstaVlaka();
            String smjer = timeTable.getSmjer();
            String polaznaStanica = timeTable.getPolaznaStanica();
            String odredisnaStanica = timeTable.getOdredisnaStanica();
            String vrijemePolaska = timeTable.getVrijemePolaska();
            String trajanjeVoznje = timeTable.getTrajanjeVoznje();
            String oznakaDana = timeTable.getOznakaDana();

            Train train = trainMap.computeIfAbsent(oznakaVlaka, k -> new Train(oznakaVlaka, vrstaVlaka));

            Railway railway = ConfigManager.getInstance().getRailwayByOznakaPruge(oznakaPruge);

            if (railway == null) {
                ConfigManager.getInstance().incrementErrorCount();
                System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Pruga s oznakom '" + oznakaPruge + "' nije pronađena. Etapa se preskače.");
                this.printFileError();
                continue;
            }

            List<Station> popisStanica = railway.getPopisSvihStanica();

            if (popisStanica.isEmpty()) {
                ConfigManager.getInstance().incrementErrorCount();
                System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nema dostupnih stanica za prugu '" + oznakaPruge + "'. Etapa se preskače.");
                this.printFileError();
                continue;
            }

            if ("O".equals(smjer)) {
                if (polaznaStanica == null || polaznaStanica.isEmpty()) {
                    polaznaStanica = popisStanica.get(popisStanica.size() - 1).getNaziv();
                }
                if (odredisnaStanica == null || odredisnaStanica.isEmpty()) {
                    odredisnaStanica = popisStanica.get(0).getNaziv();
                }
            } else {
                if (polaznaStanica == null || polaznaStanica.isEmpty()) {
                    polaznaStanica = popisStanica.get(0).getNaziv();
                }
                if (odredisnaStanica == null || odredisnaStanica.isEmpty()) {
                    odredisnaStanica = popisStanica.get(popisStanica.size() - 1).getNaziv();
                }
            }

            Etapa etapa = new Etapa(
                    oznakaVlaka,
                    oznakaPruge,
                    polaznaStanica,
                    odredisnaStanica,
                    vrijemePolaska,
                    trajanjeVoznje,
                    oznakaDana,
                    smjer
            );

            if ("O".equals(smjer)) {
                boolean addStations = false;
                for (int i = popisStanica.size() - 1; i >= 0; i--) {
                    Station currentStation = popisStanica.get(i);

                    if (currentStation.getNaziv().equals(polaznaStanica)) {
                        addStations = true;
                    }

                    if (addStations) {
                        etapa.add(new StationComposite(currentStation.getNaziv(), currentStation.getId()));
                    }

                    if (currentStation.getNaziv().equals(odredisnaStanica)) {
                        break;
                    }
                }
            } else {
                boolean addStations = false;
                for (Station currentStation : popisStanica) {
                    if (currentStation.getNaziv().equals(polaznaStanica)) {
                        addStations = true;
                    }

                    if (addStations) {
                        etapa.add(new StationComposite(currentStation.getNaziv(), currentStation.getId()));
                    }

                    if (currentStation.getNaziv().equals(odredisnaStanica)) {
                        break;
                    }
                }
            }

            List<TimeTableComponent> existingEtape = train.getChildren();
            if (!existingEtape.isEmpty()) {
                Etapa lastEtapa = (Etapa) existingEtape.get(existingEtape.size() - 1);
                if (etapa.getVrijemePolaska().compareTo(lastEtapa.getVrijemePolaska()) < 0) {
                    existingEtape.add(existingEtape.size() - 1, etapa);
                } else {
                    train.add(etapa);
                }
            } else {
                train.add(etapa);
            }


            if (!vozniRed.getChildren().contains(train)) {
                vozniRed.add(train);
            }
        }
        sortEtapeByDepartureTime();
    }

    private void removeInvalidTrains() {
        Map<String, String> trainTypes = new HashMap<>();
        List<String> invalidTrainOznake = new ArrayList<>();
        List<String> alreadyLogged = new ArrayList<>();

        for (TimeTable tt : timeTables) {
            String oznakaVlaka = tt.getOznakaVlaka();
            String vrstaVlaka = tt.getVrstaVlaka();

            if (trainTypes.containsKey(oznakaVlaka)) {
                if (!trainTypes.get(oznakaVlaka).equals(vrstaVlaka)) {
                    invalidTrainOznake.add(oznakaVlaka);
                }
            } else {
                trainTypes.put(oznakaVlaka, vrstaVlaka);
            }
        }

        for (String oznaka : invalidTrainOznake) {
            if (!alreadyLogged.contains(oznaka)) {
                ConfigManager.getInstance().incrementErrorCount();
                System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() +
                        ": Vlak s oznakom '" + oznaka + "' ima različite vrste vlaka. Svi zapisi za taj vlak su obrisani.");
                alreadyLogged.add(oznaka);
                this.printFileError();
            }
        }

        timeTables.removeIf(tt -> invalidTrainOznake.contains(tt.getOznakaVlaka()));
    }

    private String formatTime(String time) {
        if (time.matches("^\\d{1}:\\d{2}$")) {
            return "0" + time;
        }
        return time;
    }

    private void validateAndRemoveInvalidTrains(TimeTableComposite vozniRed) {
        List<TimeTableComponent> trainsToRemove = new ArrayList<>();

        for (TimeTableComponent component : vozniRed.getChildren()) {
            if (component instanceof Train train) {
                List<TimeTableComponent> etape = train.getChildren();
                boolean valid = true;

                for (int i = 0; i < etape.size() - 1; i++) {
                    Etapa currentEtapa = (Etapa) etape.get(i);
                    Etapa nextEtapa = (Etapa) etape.get(i + 1);

                    String lastStationCurrentEtapa = getLastStationName(currentEtapa);
                    String firstStationNextEtapa = getFirstStationName(nextEtapa);

                    if (!isValidTransition(lastStationCurrentEtapa, firstStationNextEtapa, currentEtapa, nextEtapa)) {
                        valid = false;
                        break;
                    }
                }

                if (!valid) {
                    trainsToRemove.add(train);
                    ConfigManager.getInstance().incrementErrorCount();
                    System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() +
                            ": Vlak s oznakom '" + train.getOznaka() + "' ima neispravnu tranziciju između etapa i bit će obrisan.");
                    this.printFileError();
                }
            }
        }

        vozniRed.getChildren().removeAll(trainsToRemove);
    }

    private String getLastStationName(Etapa etapa) {
        List<TimeTableComponent> stations = etapa.getChildren();
        return ((StationComposite) stations.get(stations.size() - 1)).getNazivStanice();
    }

    private String getFirstStationName(Etapa etapa) {
        List<TimeTableComponent> stations = etapa.getChildren();
        return ((StationComposite) stations.get(0)).getNazivStanice();
    }

    private void sortEtapeByDepartureTime() {
        for (TimeTableComponent component : vozniRed.getChildren()) {
            if (component instanceof Train train) {
                train.getChildren().sort((etapa1, etapa2) -> {
                    String vrijeme1 = ((Etapa) etapa1).getVrijemePolaska();
                    String vrijeme2 = ((Etapa) etapa2).getVrijemePolaska();
                    return vrijeme1.compareTo(vrijeme2);
                });
            }
        }
    }

    private boolean isValidTransition(String lastStation, String firstStation, Etapa currentEtapa, Etapa nextEtapa) {
        if ("N".equals(currentEtapa.getSmjer()) && "N".equals(nextEtapa.getSmjer())) {
            return lastStation.equals(firstStation);
        } else if ("N".equals(currentEtapa.getSmjer()) && "O".equals(nextEtapa.getSmjer())) {
            return lastStation.equals(firstStation);
        } else if ("O".equals(currentEtapa.getSmjer()) && "N".equals(nextEtapa.getSmjer())) {
            return lastStation.equals(firstStation);
        } else {
            return lastStation.equals(firstStation);
        }
    }
}
