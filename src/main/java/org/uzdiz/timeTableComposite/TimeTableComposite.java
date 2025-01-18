package org.uzdiz.timeTableComposite;

import java.util.ArrayList;
import java.util.List;

public abstract class TimeTableComposite extends TimeTableComponent {
    protected List<TimeTableComponent> children = new ArrayList<>();
    protected String oznaka;

    public TimeTableComposite(String oznaka) {
        this.oznaka = oznaka;
    }

    @Override
    public void showDetails() {
        for (TimeTableComponent child : this.children) {
            child.showDetails();
        }
    }

    public boolean add(TimeTableComponent component) {
        this.children.add(component);
        return true;
    }

    public boolean remove(TimeTableComponent component) {
        return this.children.remove(component);
    }

    public TimeTableComponent getChild(int i) {
        return this.children.get(i);
    }

    public String getOznaka() {
        return this.oznaka;
    }

    public List<TimeTableComponent> getChildren() {
        return children;
    }
}
