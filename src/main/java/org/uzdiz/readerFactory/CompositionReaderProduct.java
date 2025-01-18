package org.uzdiz.readerFactory;

import org.uzdiz.builder.Composition;
import org.uzdiz.ConfigManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CompositionReaderProduct implements CsvReaderProduct {
    private List<Composition> compositions = new ArrayList<>();
    private String path;
    private Integer fileErrorCounter = 0;

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

                String[] data = line.split(";");

                if (isEmptyRow(data) || !validateData(data)) {
                    continue;
                }

                Composition composition = new Composition.CompositionBuilder(data[0])
                        .setOznakaVozila(data[1])
                        .setUloga(data[2]).build();

                compositions.add(composition);
            }

            ConfigManager.getInstance().setCompositions(compositions);
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

    private boolean isEmptyRow(String[] data) {
        for (String entry : data) {
            if (entry != null && !entry.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean validateData(String[] data) {
        if (data.length != 3) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Zapis nije potpun, očekuju se 3 podatka, ali postoji " + data.length);
            this.printFileError();
            return false;
        }

        return validatePresence(data) && validateUloga(data[2], "Uloga");
    }

    private boolean validatePresence(String[] data) {
        String[] columnNames = {"Oznaka", "Oznaka vozila", "Uloga"};

        for (int i = 0; i < data.length; i++) {
            if (data[i] == null || data[i].trim().isEmpty()) {
                ConfigManager.getInstance().incrementErrorCount();
                System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Nedostaje vrijednost za stupac '" + columnNames[i] + "'.");
                this.printFileError();
                return false;
            }
        }
        return true;
    }

    private boolean validateUloga(String value, String fieldName) {
        if (!value.equals("P") && !value.equals("V")) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": " + fieldName + " mora biti 'P' ili 'V'. Pronađeno: '" + value + "'");
            this.printFileError();
            return false;
        }
        return true;
    }
}
