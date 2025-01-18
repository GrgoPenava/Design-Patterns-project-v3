package org.uzdiz.utils;

import org.uzdiz.ConfigManager;
import org.uzdiz.builder.Station;
import org.uzdiz.railwayFactory.Railway;

import java.util.*;

public class GraphUtil {
    private Map<Station, Map<Station, Double>> graph = new HashMap<>();

    public void buildGraphFromRailways() {
        List<Railway> railways = ConfigManager.getInstance().getRailways();
        for (Railway railway : railways) {
            List<Station> stations = railway.getPopisSvihStanica();
            for (int i = 0; i < stations.size() - 1; i++) {
                addEdge(stations.get(i), stations.get(i + 1), stations.get(i + 1).getDuzina());
            }
        }
        connectInterRailwayStations();
    }

    private void addEdge(Station from, Station to, double weight) {
        graph.putIfAbsent(from, new HashMap<>());
        graph.get(from).put(to, weight);
        graph.putIfAbsent(to, new HashMap<>());
        graph.get(to).put(from, weight);
    }

    private void connectInterRailwayStations() {
        Map<String, List<Station>> stationNameToStations = new HashMap<>();
        for (Railway railway : ConfigManager.getInstance().getRailways()) {
            for (Station station : railway.getPopisSvihStanica()) {
                stationNameToStations.computeIfAbsent(station.getNaziv(), k -> new ArrayList<>()).add(station);
            }
        }

        for (List<Station> stations : stationNameToStations.values()) {
            if (stations.size() > 1) {
                for (int i = 0; i < stations.size(); i++) {
                    for (int j = i + 1; j < stations.size(); j++) {
                        addEdge(stations.get(i), stations.get(j), 0);
                    }
                }
            }
        }
    }

    public Map<Station, Double> findShortestPath(String startName, String endName) {
        Station start = findStationByName(startName);
        Station end = findStationByName(endName);

        if (start == null || end == null) {
            return Collections.emptyMap();
        }

        Map<Station, Double> distances = new HashMap<>();
        PriorityQueue<Map.Entry<Station, Double>> queue = new PriorityQueue<>(Comparator.comparing(Map.Entry::getValue));
        Map<Station, Station> previous = new HashMap<>();
        Set<Station> visited = new HashSet<>();
        Map<Station, Double> result = new LinkedHashMap<>();

        for (Station vertex : graph.keySet()) {
            distances.put(vertex, Double.MAX_VALUE);
            previous.put(vertex, null);
        }

        distances.put(start, 0.0);
        queue.add(new AbstractMap.SimpleEntry<>(start, 0.0));

        while (!queue.isEmpty()) {
            Station current = queue.poll().getKey();

            if (!visited.add(current)) {
                continue;
            }

            if (current.equals(end)) {
                break;
            }

            Map<Station, Double> neighbors = graph.get(current);
            if (neighbors != null) {
                for (Map.Entry<Station, Double> neighbor : neighbors.entrySet()) {
                    Station next = neighbor.getKey();
                    double newDist = distances.get(current) + neighbor.getValue();

                    if (newDist < distances.get(next)) {
                        distances.put(next, newDist);
                        previous.put(next, current);
                        queue.add(new AbstractMap.SimpleEntry<>(next, newDist));
                    }
                }
            }
        }

        Station current = end;
        LinkedList<Station> path = new LinkedList<>();
        while (current != null && previous.containsKey(current)) {
            path.addFirst(current);
            current = previous.get(current);
        }

        for (Station station : path) {
            result.put(station, distances.get(station));
        }

        return result;
    }


    private Station findStationByName(String name) {
        return ConfigManager.getInstance().getStations().stream()
                .filter(station -> station.getNaziv().equals(name))
                .findFirst()
                .orElse(null);
    }
}
