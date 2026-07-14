package com.sneha.airbnbAppC.pricingStrategy;

import com.sneha.airbnbAppC.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);
}
