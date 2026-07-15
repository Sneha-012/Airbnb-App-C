package com.sneha.airbnbAppC.dto.property;

import com.sneha.airbnbAppC.entity.Property;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
// Property entity has no price field (price is per-room, per-day, stored in Inventory/PropertyMinPrice).
// This DTO combines property details with the computed cheapest price for search results.
public class PropertyPriceDto {

    private Property property;
    private BigDecimal price;
}
