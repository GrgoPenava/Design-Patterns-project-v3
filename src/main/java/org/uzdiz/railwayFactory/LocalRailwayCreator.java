package org.uzdiz.railwayFactory;

public class LocalRailwayCreator extends RailwayCreator {
    @Override
    public Railway factoryMethod(String oznakaPruge) {
        return new LocalRailway(oznakaPruge);
    }
}
