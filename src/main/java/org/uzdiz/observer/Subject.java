package org.uzdiz.observer;

public interface Subject {
    void attachObserver(Observer observer);

    void detachObserver(Observer observer);

    void notifyObservers(String message);
}
