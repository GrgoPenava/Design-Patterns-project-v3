package org.uzdiz.stationState;

import org.uzdiz.timeTableComposite.StationComposite;

public class ZatvorenaState implements State {
    @Override
    public void doAction(StationComposite station) {
        System.out.println("Stanica je zatvorena.");
        station.setCurrentState(this);
    }

    @Override
    public String getStatus() {
        return "Z"; // Zatvorena
    }
}
