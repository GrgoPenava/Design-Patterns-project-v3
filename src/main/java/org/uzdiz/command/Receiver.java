package org.uzdiz.command;

import java.util.ArrayList;
import java.util.List;

public class Receiver {
    private List<String> history;

    public Receiver() {
        this.history = new ArrayList<>();
    }

    public void logEntry(String message) {
        history.add(message);
    }

    public List<String> getHistory() {
        return new ArrayList<>(history);
    }

    public void clearHistory() {
        history.clear();
    }
}
