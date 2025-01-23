package org.uzdiz.timeTableComposite;

import org.uzdiz.observer.Observer;
import org.uzdiz.observer.Subject;
import org.uzdiz.stationState.*;

import java.util.ArrayList;
import java.util.List;

public class StationComposite extends TimeTableComponent implements Subject {
    private String nazivStanice;
    private Integer idStanice;
    private String status;
    private Integer brojKolosjeka;
    private List<Observer> observers = new ArrayList<>();
    private List<State> kolosijekStates = new ArrayList<>();

    public StationComposite(String nazivStanice, Integer idStanice, String status, Integer brojKolosjeka) {
        this.nazivStanice = nazivStanice;
        this.idStanice = idStanice;
        this.status = status;
        this.brojKolosjeka = brojKolosjeka;
        for (int i = 0; i < brojKolosjeka; i++) {
            this.kolosijekStates.add(this.getStateStatus(status));
        }
    }

    public void setState(int kolosijekIndex, State state) {
        if (kolosijekIndex < 0 || kolosijekIndex >= brojKolosjeka) {
            throw new IllegalArgumentException("Neispravan indeks kolosijeka.");
        }
        state.doAction(this);
        this.kolosijekStates.set(kolosijekIndex, state);
    }

    public State getState(int kolosijekIndex) {
        if (kolosijekIndex < 0 || kolosijekIndex >= brojKolosjeka) {
            throw new IllegalArgumentException("Neispravan indeks kolosijeka.");
        }
        return this.kolosijekStates.get(kolosijekIndex);
    }

    public void setCurrentState(State state) {
        // Za potrebe stanja radimo s jednim kolosijekom ako je potrebno
    }

    @Override
    public void showDetails() {
        System.out.println(nazivStanice);
    }

    public Integer getIdStanice() {
        return idStanice;
    }

    public String getNazivStanice() {
        return nazivStanice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getBrojKolosjeka() {
        return brojKolosjeka;
    }

    @Override
    public void attachObserver(Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void detachObserver(Observer observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObservers(String message) {
        for (Observer observer : this.observers) {
            observer.update(message);
        }
    }

    public boolean hasObserver(Observer observer) {
        return observers.contains(observer);
    }

    private State getStateStatus(String statusPruge) {
        return switch (statusPruge) {
            case "I" -> new IspravnaState();
            case "K" -> new KvarState();
            case "T" -> new TestiranjeState();
            case "Z" -> new ZatvorenaState();
            default -> new IspravnaState();
        };
    }

}
