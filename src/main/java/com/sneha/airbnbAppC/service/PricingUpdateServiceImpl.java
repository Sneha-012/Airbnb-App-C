package com.sneha.airbnbAppC.service;

import com.sneha.airbnbAppC.entity.Inventory;
import com.sneha.airbnbAppC.entity.Property;
import com.sneha.airbnbAppC.entity.PropertyMinPrice;
import com.sneha.airbnbAppC.pricingStrategy.PricingService;
import com.sneha.airbnbAppC.repository.InventoryRepository;
import com.sneha.airbnbAppC.repository.PropertyMinPriceRepository;
import com.sneha.airbnbAppC.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PricingUpdateServiceImpl implements PricingUpdateService{

    private final PropertyRepository propertyRepository;
    private final InventoryRepository inventoryRepository;
    private final PropertyMinPriceRepository propertyMinPriceRepository;
    private final PricingService pricingService;

    //Scheduler to update the inventory and PropertyMinPrice table every hour

    /**
      Runs automatically at the top of every hour (e.g. 1:00, 2:00, 3:00...).
      Recalculates dynamic pricing for every property's upcoming year of inventory,
      then refreshes each property's "starting from" minimum price used on search results.
      This keeps prices fresh without recalculating them live on every search request.
     */
    @Override
    @Scheduled(cron = "0 0 * * * *")
    public void updatesPrices(){
        log.info("Starting scheduled pricing update job");
        int page  = 0;
        int batchSize = 100;

        while(true){
            Page<Property> propertyPage = propertyRepository.findAll(PageRequest.of(page, batchSize));
            if(propertyPage.isEmpty()){
                break;
            }
            propertyPage.getContent().forEach(this::updatePropertyPrices);
            page++;
        }
    }

    private void updatePropertyPrices(Property property){
        log.info("Updating prices for property id: {}", property.getId());

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusYears(1);

        List<Inventory> inventoryList = inventoryRepository.findByPropertyAndDateBetween(property,startDate,endDate);

        log.info("Found {} inventory rows to reprice for property id: {}", inventoryList.size(), property.getId());

        updateInventoryPrices(inventoryList);
        updatePropertyMinPrice(property,inventoryList,startDate,endDate);

        log.info("Finished updating prices for property id: {}", property.getId());

    }

    private void updateInventoryPrices(List<Inventory> inventoryList){
        log.debug("Recalculating dynamic price for {} inventory rows", inventoryList.size());

        inventoryList.forEach(inventory -> {
            BigDecimal dynamicPrice = pricingService.calculateDynamicPricing(inventory);
            inventory.setPrice(dynamicPrice);
        });

        inventoryRepository.saveAll(inventoryList);
        log.debug("Saved {} repriced inventory rows", inventoryList.size());

    }

    private void updatePropertyMinPrice(Property property, List<Inventory> inventoryList,
                                        LocalDate startDate, LocalDate endDate){

        log.debug("Calculating daily minimum prices for property id: {}", property.getId());

        Map<LocalDate, BigDecimal> dailyMinPrices = new HashMap<>();

        for (Inventory inventory : inventoryList) {
            LocalDate date = inventory.getDate();
            BigDecimal price = inventory.getPrice();

            BigDecimal currentMin = dailyMinPrices.get(date);

            if (currentMin == null || price.compareTo(currentMin) < 0) {
                dailyMinPrices.put(date, price);
            }
        }

        List<PropertyMinPrice> propertyPrices = new ArrayList<>();
        dailyMinPrices.forEach((date, price) -> {
            PropertyMinPrice propertyPrice = propertyMinPriceRepository.findByPropertyAndDate(property, date)
                    .orElse(new PropertyMinPrice(property, date));
            propertyPrice.setPrice(price);
            propertyPrices.add(propertyPrice);
        });

        propertyMinPriceRepository.saveAll(propertyPrices);
        log.info("Updated {} min-price rows for property id: {}", propertyPrices.size(), property.getId());
    }

}
