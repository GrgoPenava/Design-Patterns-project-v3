package org.uzdiz.strategy;

public interface PriceCalculationStrategy {
    double calculatePrice(double basePrice, boolean isWeekend);
}
