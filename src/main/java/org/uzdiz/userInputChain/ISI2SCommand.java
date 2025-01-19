package org.uzdiz.userInputChain;

import org.uzdiz.ConfigManager;
import org.uzdiz.railwayFactory.Railway;
import org.uzdiz.builder.Station;
import org.uzdiz.utils.TableBuilder;
import org.uzdiz.utils.GraphUtil;

import java.util.*;

public class ISI2SCommand extends CommandHandlerChain {
    private GraphUtil graphUtil = new GraphUtil();

    @Override
    protected boolean canHandle(String input) {
        return input.matches("^ISI2S(\\s|$).*");
    }

    @Override
    public void execute(String input) {
        graphUtil.buildGraphFromRailways();
        if (!validateInput(input)) {
            return;
        }

        String[] parts = input.substring(6).split(" - ");
        String startStation = parts[0].trim();
        String endStation = parts[1].trim();

        if (!stationExists(startStation) || !stationExists(endStation)) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Jedna ili obje stanice ne postoje u sustavu.");
            return;
        }

        Optional<Railway> railwayOptional = ConfigManager.getInstance().getRailways().stream()
                .filter(railway -> containsStations(railway, startStation, endStation))
                .findFirst();

        if (railwayOptional.isEmpty()) {
            Map<Station, Double> path = graphUtil.findShortestPath(startStation, endStation);
            Map<Station, Double> filteredPath = filterDuplicateStations(path);

            TableBuilder table = new TableBuilder();
            table.setHeaders("Naziv stanice", "Vrsta", "Broj km od početne stanice");

            for (Map.Entry<Station, Double> entry : filteredPath.entrySet()) {
                table.addRow(entry.getKey().getNaziv(), entry.getKey().getVrstaStanice(), String.format("%.2f", entry.getValue()));
            }

            table.build();
            return;
        }

        Railway railway = railwayOptional.get();
        List<Station> stations = railway.getPopisSvihStanica();

        int startIndex = findStationIndex(stations, startStation);
        int endIndex = findStationIndex(stations, endStation);

        if (startIndex == -1 || endIndex == -1 || startIndex == endIndex) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Stanice nisu ispravne ili se ne nalaze na istoj pruzi.");
            return;
        }

        TableBuilder table = new TableBuilder();
        table.setHeaders("Naziv stanice", "Vrsta", "Broj km od početne stanice");

        if (startIndex < endIndex) {
            printNormalOrder(table, stations, startIndex, endIndex);
        } else {
            printReverseOrder(table, stations, startIndex, endIndex);
        }

        table.build();
    }

    private void printNormalOrder(TableBuilder table, List<Station> stations, int startIndex, int endIndex) {
        List<Station> withoutDuplicates = new ArrayList<>();
        for (int i = startIndex; i <= endIndex; i++) {
            Station currentStation = stations.get(i);

            boolean hasDuplicateWithNonZeroLength = stations.subList(startIndex, endIndex).stream()
                    .anyMatch(station -> station.getNaziv().equals(currentStation.getNaziv()) && station.getDuzina() > 0);

            if (currentStation.getDuzina() == 0 && hasDuplicateWithNonZeroLength) {
                continue;
            }

            withoutDuplicates.add(currentStation);
        }

        double distanceSum = 0;
        for (int i = 0; i <= withoutDuplicates.size() - 1; i++) {
            if (i != 0) {
                distanceSum += withoutDuplicates.get(i).getDuzina();
            }
            table.addRow(withoutDuplicates.get(i).getNaziv(), withoutDuplicates.get(i).getVrstaStanice(), String.format("%.2f", distanceSum));
        }
    }

    private void printReverseOrder(TableBuilder table, List<Station> stations, int startIndex, int endIndex) {
        List<Station> withoutDuplicates = new ArrayList<>();
        for (int i = endIndex; i < startIndex + 1; i++) {
            Station currentStation = stations.get(i);

            boolean hasDuplicateWithNonZeroLength = stations.subList(endIndex, startIndex).stream()
                    .anyMatch(station -> station.getNaziv().equals(currentStation.getNaziv()) && station.getDuzina() > 0);

            if (currentStation.getDuzina() == 0 && hasDuplicateWithNonZeroLength) {
                continue;
            }

            withoutDuplicates.add(currentStation);
        }

        double distanceSum = 0;
        for (int i = withoutDuplicates.size(); i > 0; i--) {
            Station station = withoutDuplicates.get(i - 1);

            if (i < withoutDuplicates.size()) {
                distanceSum += withoutDuplicates.get(i).getDuzina();
            }
            table.addRow(station.getNaziv(), station.getVrstaStanice(), String.format("%.2f", distanceSum));
        }
    }

    private Map<Station, Double> filterDuplicateStations(Map<Station, Double> stationDistances) {
        Map<String, Station> uniqueStations = new LinkedHashMap<>();
        Map<Station, Double> filteredMap = new LinkedHashMap<>();

        for (Map.Entry<Station, Double> entry : stationDistances.entrySet()) {
            Station currentStation = entry.getKey();
            String stationName = currentStation.getNaziv();

            if (!uniqueStations.containsKey(stationName)) {
                uniqueStations.put(stationName, currentStation);
                filteredMap.put(currentStation, entry.getValue());
            } else {
                Station existingStation = uniqueStations.get(stationName);
                if (currentStation.getDuzina() > existingStation.getDuzina()) {
                    uniqueStations.put(stationName, currentStation);
                    filteredMap.remove(existingStation);
                    filteredMap.put(currentStation, entry.getValue());
                }
            }
        }

        return filteredMap;
    }


    private boolean validateInput(String input) {
        if (!input.contains(" - ")) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Naredba mora sadržavati početnu stanicu, znak ' - ' i završnu stanicu.");
            return false;
        }

        String[] stationParts = input.substring(6).split(" - ");
        if (stationParts.length != 2 || stationParts[0].trim().isEmpty() || stationParts[1].trim().isEmpty()) {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() + ": Unesite ispravno ime početne i završne stanice.");
            return false;
        }
        return true;
    }

    private boolean containsStations(Railway railway, String startStation, String endStation) {
        return railway.getPopisSvihStanica().stream().anyMatch(station -> station.getNaziv().equals(startStation)) &&
                railway.getPopisSvihStanica().stream().anyMatch(station -> station.getNaziv().equals(endStation));
    }

    private int findStationIndex(List<Station> stations, String stationName) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.get(i).getNaziv().equals(stationName)) {
                return i;
            }
        }
        return -1;
    }

    private boolean stationExists(String stationName) {
        return ConfigManager.getInstance().getStations().stream()
                .anyMatch(station -> station.getNaziv().equals(stationName));
    }
}
