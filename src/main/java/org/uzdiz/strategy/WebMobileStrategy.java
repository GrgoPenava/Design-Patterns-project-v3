package org.uzdiz.strategy;

import org.uzdiz.ConfigManager;

public class WebMobileStrategy implements PriceCalculationStrategy {
    @Override
    public double calculatePrice(double basePrice, boolean isWeekend) {
        ConfigManager configManager = ConfigManager.getInstance();
        double price = basePrice - (basePrice * (configManager.getTicketPrice().popustWebMob / 100));

        if (isWeekend) {
            price = price - ((configManager.getTicketPrice().popustSuN / 100) * price);
        }
        return price;
    }
}
