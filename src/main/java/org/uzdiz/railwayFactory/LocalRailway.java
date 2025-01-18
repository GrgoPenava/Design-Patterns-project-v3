package org.uzdiz.railwayFactory;

public class LocalRailway extends Railway {
    public LocalRailway(String oznakaPruge) {
        super(oznakaPruge);
        this.kategorija = "Lokalna";
    }
}
