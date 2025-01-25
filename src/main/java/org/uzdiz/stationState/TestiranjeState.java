package org.uzdiz.stationState;

import org.uzdiz.timeTableComposite.StationComposite;

public class TestiranjeState implements State {
    @Override
    public void doAction(StationComposite station) {
        //System.out.println("Stanica je u stanju testiranja.");
        station.setCurrentState(this);
    }

    @Override
    public String getStatus() {
        return "T"; // Testiranje
    }

    @Override
    public String getStatusName() {
        return "Testiranje";
    }
}
