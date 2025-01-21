package org.uzdiz.stationState;

import org.uzdiz.timeTableComposite.StationComposite;

public class KvarState implements State {
    @Override
    public void doAction(StationComposite station) {
        System.out.println("Stanica je u kvaru." + station.getNazivStanice());
        station.setCurrentState(this);
    }

    @Override
    public String getStatus() {
        return "K"; // Kvar
    }
}
