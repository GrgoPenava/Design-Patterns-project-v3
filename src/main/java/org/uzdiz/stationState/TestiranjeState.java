package org.uzdiz.stationState;

import org.uzdiz.timeTableComposite.StationComposite;

public class TestiranjeState implements State {
    @Override
    public void doAction(StationComposite station) {
        station.setCurrentState(this);
    }

    @Override
    public String getStatus() {
        return "T";
    }

    @Override
    public String getStatusName() {
        return "Testiranje";
    }
}
