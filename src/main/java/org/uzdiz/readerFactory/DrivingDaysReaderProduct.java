package org.uzdiz.readerFactory;

import org.uzdiz.DrivingDays;
import org.uzdiz.ConfigManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DrivingDaysReaderProduct implements CsvReaderProduct {
    private List<DrivingDays> drivingDaysList = new ArrayList<>();
    private String path;
    private Integer fileErrorCounter = 0;

    private static final List<String> VALID_DAYS = Arrays.asList("Po", "U", "Sr", "Č", "Pe", "Su", "N");

    @Override
    public void loadData(String filePath) {
        this.path = filePath;
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

                String oznaka = data[0].trim();
                List<String> days = parseDays(data[1].trim());

                if (days == null) {
                    continue;
                }

                DrivingDays drivingDays = new DrivingDays(oznaka, days);
                drivingDaysList.add(drivingDays);
            }

            ConfigManager.getInstance().setDrivingDaysList(drivingDaysList);
        } catch (IOException e) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nije moguće učitati datoteku - " + filePath);
            this.printFileError();
        }
    }

    private List<String> parseDays(String daysString) {
        List<String> result = new ArrayList<>();
        int index = 0;

        while (index < daysString.length()) {
            boolean matched = false;

            for (String day : VALID_DAYS) {
                if (daysString.startsWith(day, index)) {
                    result.add(day);
                    index += day.length();
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                ConfigManager.getInstance().incrementErrorCount();
                System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() +
                        ": Neispravna oznaka za 'Dani vožnje' u zapisu: '" + daysString + "'");
                this.printFileError();
                return null;
            }
        }

        return result;
    }

    private void printFileError() {
        this.fileErrorCounter++;
        System.out.println("-> Greška datoteke (" + path + ") br. " + this.fileErrorCounter);
    }

    private boolean validateData(String[] data) {
        if (data.length != 2) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Zapis nije potpun, očekuje se 2 podatka, ali postoji " + data.length);
            this.printFileError();
            return false;
        }

        if (data[0].trim().isEmpty()) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nedostaje vrijednost za 'Oznaka'.");
            this.printFileError();
            return false;
        }

        return true;
    }
}
