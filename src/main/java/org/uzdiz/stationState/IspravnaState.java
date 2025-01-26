package org.uzdiz.stationState;

import org.uzdiz.timeTableComposite.StationComposite;

public class IspravnaState implements State {
    @Override
    public void doAction(StationComposite station) {
        station.setCurrentState(this);
    }

    @Override
    public String getStatus() {
        return "I";
    }

    @Override
    public String getStatusName() {
        return "Ispravna";
    }
}
