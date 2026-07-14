package com.sneha.airbnbAppC.pricingStrategy;

import com.sneha.airbnbAppC.entity.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BasePricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        return inventory.getRoom().getBasePrice();
        //for base price we need to look into room
    }
}
