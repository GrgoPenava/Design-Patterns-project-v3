package org.uzdiz.stationState;

import org.uzdiz.timeTableComposite.StationComposite;

public class KvarState implements State {
    @Override
    public void doAction(StationComposite station) {
        station.setCurrentState(this);
    }

    @Override
    public String getStatus() {
        return "K";
    }

    @Override
    public String getStatusName() {
        return "Kvar";
    }
}
