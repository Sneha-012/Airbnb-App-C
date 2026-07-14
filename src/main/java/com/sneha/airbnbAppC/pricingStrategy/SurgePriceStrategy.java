package com.sneha.airbnbAppC.pricingStrategy;

import com.sneha.airbnbAppC.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SurgePriceStrategy implements PricingStrategy{

    private final PricingStrategy wrapped;    //wrapping the base price

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {

        BigDecimal price = wrapped.calculatePrice(inventory);
        return price.multiply(inventory.getSurgeFactor());
    }
}
