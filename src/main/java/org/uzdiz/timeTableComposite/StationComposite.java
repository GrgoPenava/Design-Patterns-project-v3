package org.uzdiz.timeTableComposite;

import org.uzdiz.observer.Observer;
import org.uzdiz.observer.Subject;

import java.util.ArrayList;
import java.util.List;

public class StationComposite extends TimeTableComponent implements Subject {
    private String nazivStanice;
    private Integer idStanice;
    private List<Observer> observers = new ArrayList<>();

    public StationComposite(String nazivStanice, Integer idStanice) {
        this.nazivStanice = nazivStanice;
        this.idStanice = idStanice;
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

}
