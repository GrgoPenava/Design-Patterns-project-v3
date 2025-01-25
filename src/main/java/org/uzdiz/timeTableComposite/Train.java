package org.uzdiz.timeTableComposite;

import org.uzdiz.observer.Observer;
import org.uzdiz.observer.Subject;
import org.uzdiz.user.User;

import java.util.ArrayList;
import java.util.List;

public class Train extends TimeTableComposite implements Subject {
    private String vrstaVlaka;
    private List<Observer> observers = new ArrayList<>();
    private List<User> passengers = new ArrayList<>();

    public Train(String oznakaVlaka, String vrstaVlaka) {
        super(oznakaVlaka);
        this.vrstaVlaka = vrstaVlaka;
    }

    @Override
    public boolean add(TimeTableComponent component) {
        return super.add(component);
    }

    public String getVrstaVlaka() {
        return vrstaVlaka;
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

    public boolean addPassenger(User user) {
        if (!passengers.contains(user)) {
            passengers.add(user);
            return true;
        }
        return false;
    }

    public boolean removePassenger(User user) {
        return passengers.remove(user);
    }

    public List<User> getPassengers() {
        return new ArrayList<>(passengers);
    }

}
