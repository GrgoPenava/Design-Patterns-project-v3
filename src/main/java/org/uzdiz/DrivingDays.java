package org.uzdiz;

import java.util.List;

public class DrivingDays {
    private String oznaka;
    private List<String> days;

    public DrivingDays(String oznaka, List<String> days) {
        this.oznaka = oznaka;
        this.days = days;
    }

    public String getOznaka() {
        return oznaka;
    }

    public List<String> getDays() {
        return days;
    }
}
