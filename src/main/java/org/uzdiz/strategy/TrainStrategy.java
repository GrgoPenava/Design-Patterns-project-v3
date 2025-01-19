package org.uzdiz.strategy;

import org.uzdiz.ConfigManager;

public class TrainStrategy implements PriceCalculationStrategy{
    @Override
    public double calculatePrice(double basePrice, boolean isWeekend) {
        ConfigManager configManager = ConfigManager.getInstance();
        // Tu radim izracun cijene karte
        double price = basePrice + (basePrice * (configManager.getTicketPrice().uvecanjeVlak / 100));

        // Gledam vikend
        if (isWeekend) {
            price = price - ((configManager.getTicketPrice().popustSuN / 100) * price);
        }
        return price;
    }
}
