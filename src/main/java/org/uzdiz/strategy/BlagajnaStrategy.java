package org.uzdiz.strategy;

import org.uzdiz.ConfigManager;

public class BlagajnaStrategy implements PriceCalculationStrategy{
    @Override
    public double calculatePrice(double basePrice, boolean isWeekend) {
        ConfigManager configManager = ConfigManager.getInstance();
        double price = basePrice;

        if (isWeekend) {
            price = price - ((configManager.getTicketPrice().popustSuN / 100) * price);
        }
        return price;
    }
}
