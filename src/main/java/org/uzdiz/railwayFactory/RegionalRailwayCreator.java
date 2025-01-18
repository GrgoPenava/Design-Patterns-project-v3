package org.uzdiz.railwayFactory;

public class RegionalRailwayCreator extends RailwayCreator {
    @Override
    public Railway factoryMethod(String oznakaPruge) {
        return new RegionalRailway(oznakaPruge);
    }
}
