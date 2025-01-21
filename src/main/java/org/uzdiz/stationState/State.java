package org.uzdiz.stationState;

import org.uzdiz.timeTableComposite.StationComposite;

public interface State {
    void doAction(StationComposite station);

    String getStatus();
}
