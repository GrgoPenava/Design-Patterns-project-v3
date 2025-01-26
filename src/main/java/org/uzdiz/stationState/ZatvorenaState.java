package org.uzdiz.stationState;

import org.uzdiz.timeTableComposite.StationComposite;

public class ZatvorenaState implements State {
    @Override
    public void doAction(StationComposite station) {
        station.setCurrentState(this);
    }

    @Override
    public String getStatus() {
        return "Z";
    }

    @Override
    public String getStatusName() {
        return "Zatvorena";
    }
}
