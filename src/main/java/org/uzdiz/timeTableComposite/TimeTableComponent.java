package org.uzdiz.timeTableComposite;

public abstract class TimeTableComponent {
    public abstract void showDetails();

    public boolean add(TimeTableComponent component) {
        if (!(this instanceof TimeTableComposite)) {
            return false;
        }
        return this.add(component);
    }

    public boolean remove(TimeTableComponent component) {
        if (!(this instanceof TimeTableComposite)) {
            return false;
        }
        return this.remove(component);
    }

    public TimeTableComponent getChild(int i) {
        if (!(this instanceof TimeTableComposite)) {
            return null;
        }
        return this.getChild(i);
    }
}
