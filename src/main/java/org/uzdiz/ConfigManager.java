package org.uzdiz;

import org.uzdiz.builder.Composition;
import org.uzdiz.builder.Station;
import org.uzdiz.builder.TimeTable;
import org.uzdiz.builder.Vehicle;
import org.uzdiz.mediator.NotificationMediatorImpl;
import org.uzdiz.railwayFactory.Railway;
import org.uzdiz.timeTableComposite.TimeTableComposite;
import org.uzdiz.user.User;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static volatile ConfigManager instance;

    private NotificationMediatorImpl mediator = new NotificationMediatorImpl();

    private String stationFilePath;
    private String railwayFilePath;
    private String compositionFilePath;
    private String timeTableFilePath;
    private String drivingDaysFilePath;

    private List<Station> stations = new ArrayList<>();
    private List<Railway> railways = new ArrayList<>();
    private List<Composition> compositions = new ArrayList<>();
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<TimeTable> timeTables = new ArrayList<>();
    private List<DrivingDays> drivingDays = new ArrayList<>();
    private List<User> users = new ArrayList<>();

    private TimeTableComposite vozniRed;

    private int errorCount;

    private ConfigManager() {
        this.errorCount = 0;
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    public void setStationFilePath(String path) {
        this.stationFilePath = path;
    }

    public String getStationFilePath() {
        return stationFilePath;
    }

    public void setRailwayFilePath(String path) {
        this.railwayFilePath = path;
    }

    public String getRailwayFilePath() {
        return railwayFilePath;
    }

    public void setCompositionFilePath(String path) {
        this.compositionFilePath = path;
    }

    public String getCompositionFilePath() {
        return compositionFilePath;
    }

    public void incrementErrorCount() {
        this.errorCount++;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    public List<Station> getStations() {
        return stations;
    }

    public void setRailways(List<Railway> railways) {
        this.railways = railways;
    }

    public List<Railway> getRailways() {
        return railways;
    }

    public void setCompositions(List<Composition> compositions) {
        this.compositions = compositions;
    }

    public List<Composition> getCompositions() {
        return compositions;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public String getTimeTableFilePath() {
        return timeTableFilePath;
    }

    public void setTimeTableFilePath(String timeTableFilePath) {
        this.timeTableFilePath = timeTableFilePath;
    }

    public String getDrivingDaysFilePath() {
        return drivingDaysFilePath;
    }

    public void setDrivingDaysFilePath(String drivingDaysFilePath) {
        this.drivingDaysFilePath = drivingDaysFilePath;
    }

    public void setTimeTables(List<TimeTable> timeTables) {
        this.timeTables = timeTables;
    }

    public void setDrivingDaysList(List<DrivingDays> drivingDaysList) {
        this.drivingDays = drivingDaysList;
    }

    public List<DrivingDays> getDrivingDaysList() {
        return drivingDays;
    }

    public List<TimeTable> getTimeTables() {
        return timeTables;
    }

    public NotificationMediatorImpl getMediator() {
        return mediator;
    }

    public void setVozniRed(TimeTableComposite vozniRed) {
        this.vozniRed = vozniRed;
    }

    public TimeTableComposite getVozniRed() {
        return vozniRed;
    }

    public List<DrivingDays> getDrivingDays() {
        return drivingDays;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public List<User> getUsers() {
        return users;
    }

    public Railway getRailwayByOznakaPruge(String oznakaPruge) {
        return railways.stream()
                .filter(r -> r.getOznakaPruge().equals(oznakaPruge))
                .findFirst()
                .orElse(null);
    }


}
