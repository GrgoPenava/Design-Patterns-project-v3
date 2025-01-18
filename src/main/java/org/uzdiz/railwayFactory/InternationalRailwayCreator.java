package org.uzdiz.railwayFactory;

public class InternationalRailwayCreator extends RailwayCreator {
    @Override
    public Railway factoryMethod(String oznakaPruge) {
        return new InternationalRailway(oznakaPruge);
    }
}
