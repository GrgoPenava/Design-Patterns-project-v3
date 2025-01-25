package org.uzdiz.stationState;

import org.uzdiz.timeTableComposite.StationComposite;

public class IspravnaState implements State {
    @Override
    public void doAction(StationComposite station) {
        //System.out.println("Stanica je u ispravnom stanju.");
        station.setCurrentState(this);
    }

    @Override
    public String getStatus() {
        return "I"; // Ispravna
    }

    @Override
    public String getStatusName() {
        return "Ispravna";
    }
}
