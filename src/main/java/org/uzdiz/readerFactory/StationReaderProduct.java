package org.uzdiz.readerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.uzdiz.ConfigManager;
import org.uzdiz.railwayFactory.*;
import org.uzdiz.builder.Station;

public class StationReaderProduct implements CsvReaderProduct {
    private List<Station> stations = new ArrayList<>();
    private String path;
    private Integer fileErrorCounter = 0;

    @Override
    public void loadData(String filePath) {
        this.path = filePath;
        File file = new File(filePath);
        if (!file.exists()) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nije moguće učitati datoteku - " + filePath);
            this.printFileError();
            return;
        }

        List<Railway> railways = new ArrayList<>();
        Railway currentRailway = null;
        String currentRailwayType = null;
        Integer idCounter = 0;

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

                try {
                    String oznakaPruge = data[1];
                    Station.StationBuilder stationBuilder = new Station.StationBuilder().setObavezniParametri(idCounter, data[0], data[1], data[2], data[3],
                            "DA".equals(data[4]),
                            "DA".equals(data[5]),
                            data[6],
                            Integer.parseInt(data[7]),
                            data[8],
                            Integer.parseInt(data[9]),
                            Double.parseDouble(data[10].replace(",", ".")),
                            Double.parseDouble(data[11].replace(",", ".")),
                            data[12],
                            Integer.parseInt(data[13]));

                    if (!data[14].isEmpty()) {
                        stationBuilder.setVrijemeNormalniVlak(Integer.parseInt(data[14]));
                    }
                    if (!data[15].isEmpty()) {
                        stationBuilder.setVrijemeUbrzaniVlak(Integer.parseInt(data[15]));
                    }
                    if (!data[16].isEmpty()) {
                        stationBuilder.setVrijemeBrziVlak(Integer.parseInt(data[16]));
                    }

                    Station station = stationBuilder.build();
                    idCounter++;

                    if (currentRailway == null || !oznakaPruge.equals(currentRailwayType)) {
                        if (currentRailway != null) {
                            railways.add(currentRailway);
                        }
                        currentRailway = this.createRailway(data[6], oznakaPruge);
                        currentRailwayType = oznakaPruge;
                    }

                    currentRailway.addStation(station);
                    stations.add(station);
                } catch (IllegalArgumentException e) {
                    System.out.println("Nepodrzan zapis: " + line + " (" + e.getMessage() + ")");
                    this.printFileError();
                }
            }

            if (currentRailway != null) railways.add(currentRailway);

            ConfigManager.getInstance().setRailways(railways);
            ConfigManager.getInstance().setStations(stations);
        } catch (IOException e) {
            System.out.println("Greška pri čitanju stanica datoteke: " + filePath);
            this.printFileError();
        }
    }

    private void printFileError() {
        this.fileErrorCounter++;
        System.out.println("-> Greška datoteke (" + path + ") br. " + this.fileErrorCounter);
    }

    public Railway createRailway(String type, String oznakaPruge) {
        RailwayCreator creator;

        char railType = type.charAt(0);
        switch (railType) {
            case 'L':
                creator = new LocalRailwayCreator();
                break;
            case 'R':
                creator = new RegionalRailwayCreator();
                break;
            case 'M':
                creator = new InternationalRailwayCreator();
                break;
            default:
                throw new IllegalArgumentException("Nepoznat tip pruge: " + type);
        }

        return creator.createRailway(oznakaPruge);
    }

    private boolean validateData(String[] data) {
        if (isEmptyRow(data) || isInvalidLength(data)) {
            return false;
        }
        if (!validateEntriesExist(data) || !validateStationType(data[2]) ||
                !validateStationStatus(data[3]) || !validatePassengerLoad(data[4]) ||
                !validateCargoLoad(data[5]) || !validateRailwayCategory(data[6]) ||
                !validatePeronNumber(data[7]) || !validateTrackNumber(data[9]) ||
                !validateAxleLoad(data[10]) || !validateMeterLoad(data[11]) ||
                !validateRailwayStatus(data[12]) || !validateLength(data[13])) {
            return false;
        }
        return true;
    }

    private boolean isEmptyRow(String[] data) {
        boolean isEmptyRow = true;
        for (String entry : data) {
            if (entry != null && !entry.isEmpty()) {
                isEmptyRow = false;
                break;
            }
        }
        return isEmptyRow;
    }

    private boolean isInvalidLength(String[] data) {
        if (data.length != 17) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Zapis nije potpun, očekuje se 14 podataka, ali postoji " + data.length);
            this.printFileError();
            return true;
        }
        return false;
    }

    private boolean validateEntriesExist(String[] data) {
        String[] columnNames = {"Stanica", "Oznaka pruge", "Vrsta stanice", "Status stanice", "Putnici ul/iz",
                "Roba ut/ist", "Kategorija pruge", "Broj perona", "Vrsta pruge", "Broj kolosjeka",
                "DO po osovini", "DO po dužnom metru", "Status pruge", "Dužina"};

        for (int i = 0; i <= 14; i++) {
            if (data[i] == null || data[i].isEmpty()) {
                ConfigManager.getInstance().incrementErrorCount();
                System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nedostaje vrijednost za stupac '" + columnNames[i] + "'.");
                this.printFileError();
                return false;
            }
        }
        return true;
    }

    private boolean validateStationType(String stationType) {
        if (!stationType.equals("kol.") && !stationType.equals("staj.")) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Vrsta stanice mora biti 'kol.' ili 'staj.'. '" + stationType + "' zapis nije podržan.");
            this.printFileError();
            return false;
        }
        return true;
    }

    private boolean validateStationStatus(String stationStatus) {
        if (!stationStatus.equals("O") && !stationStatus.equals("Z")) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Status stanice mora biti 'O' ili 'Z'. '" + stationStatus + "' nije podržana vrijednost.");
            this.printFileError();
            return false;
        }
        return true;
    }

    private boolean validatePassengerLoad(String passengerLoad) {
        if (!passengerLoad.equals("DA") && !passengerLoad.equals("NE")) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Putnici ul/iz mora biti 'DA' ili 'NE'. '" + passengerLoad + "' nije podržana vrijednost.");
            this.printFileError();
            return false;
        }
        return true;
    }

    private boolean validateCargoLoad(String cargoLoad) {
        if (!cargoLoad.equals("DA") && !cargoLoad.equals("NE")) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Roba ut/ist mora biti 'DA' ili 'NE'. '" + cargoLoad + "' nije podržana vrijednost.");
            this.printFileError();
            return false;
        }
        return true;
    }

    private boolean validateRailwayCategory(String category) {
        if (!category.equals("M") && !category.equals("L") && !category.equals("R")) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Kategorija pruge mora biti 'M', 'L' ili 'R'. '" + category + "' nije podržana vrijednost.");
            this.printFileError();
            return false;
        }
        return true;
    }

    private boolean validatePeronNumber(String peron) {
        try {
            Integer.parseInt(peron);
        } catch (NumberFormatException e) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Broj perona mora biti cijeli broj. '" + peron + "' nije ispravan broj.");
            this.printFileError();
            return false;
        }
        return true;
    }

    private boolean validateTrackNumber(String track) {
        try {
            Integer.parseInt(track);
        } catch (NumberFormatException e) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Broj kolosjeka mora biti cijeli broj. '" + track + "' nije ispravan broj.");
            this.printFileError();
            return false;
        }
        return true;
    }

    private boolean validateAxleLoad(String axleLoad) {
        if (!isDecimalOrInteger(axleLoad)) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": DO po osovini mora biti cijeli broj ili decimalni broj sa zarezom. '" + axleLoad + "' nije ispravna vrijednost.");
            this.printFileError();
            return false;
        }
        return true;
    }

    private boolean validateMeterLoad(String meterLoad) {
        if (!isDecimalOrInteger(meterLoad)) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": DO po dužnom metru mora biti cijeli broj ili decimalni broj sa zarezom. '" + meterLoad + "' nije ispravna vrijednost.");
            this.printFileError();
            return false;
        }
        return true;
    }

    private boolean validateRailwayStatus(String status) {
        if (!status.equals("I") && !status.equals("K") && !status.equals("Z") && !status.equals("T")) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Status pruge mora biti 'I', 'K', 'T' ili 'Z'. '" + status + "' nije podržana vrijednost.");
            this.printFileError();
            return false;
        }
        return true;
    }

    private boolean validateLength(String length) {
        if (!isDecimalOrInteger(length)) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Duljina mora biti cijeli broj ili decimalni broj. '" + length + "' nije ispravna vrijednost.");
            this.printFileError();
            return false;
        }
        return true;
    }

    private boolean isDecimalOrInteger(String value) {
        return Pattern.matches("\\d+(,\\d+)?", value);
    }
}
