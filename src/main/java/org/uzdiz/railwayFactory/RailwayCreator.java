package org.uzdiz.railwayFactory;

public abstract class RailwayCreator {
    public abstract Railway factoryMethod(String oznakaPruge);

    public Railway createRailway(String oznakaPruge) {
        return factoryMethod(oznakaPruge);
    }
}
