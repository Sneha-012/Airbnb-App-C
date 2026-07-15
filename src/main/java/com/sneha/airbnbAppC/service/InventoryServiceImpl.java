package com.sneha.airbnbAppC.service;

import com.sneha.airbnbAppC.dto.property.PropertyPriceDto;
import com.sneha.airbnbAppC.dto.property.PropertyResponseDto;
import com.sneha.airbnbAppC.dto.property.PropertySearchRequestDto;
import com.sneha.airbnbAppC.entity.Inventory;
import com.sneha.airbnbAppC.entity.Property;
import com.sneha.airbnbAppC.entity.Room;
import com.sneha.airbnbAppC.repository.InventoryRepository;
import com.sneha.airbnbAppC.repository.PropertyMinPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService{

    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final PropertyMinPriceRepository propertyMinPriceRepository;

    @Override
    @Transactional
    public void initializeRoomForAYear(Room room) {

        log.info("Initializing a year of inventory for room id: {}", room.getId());
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);
        for (; !today.isAfter(endDate); today = today.plusDays(1)){
            Inventory inventory = Inventory.builder()
                    .property(room.getProperty())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .city(room.getProperty().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();
            inventoryRepository.save(inventory);


        }
        //log.info("Created inventory rows for room id: {}", inventoryRepository.size(), room.getId());
    }

    @Override
    public void deleteFutureInventories(Room room) {
        log.info("Deleting future inventories for room id: {}", room.getId());
        LocalDate today = LocalDate.now();
        inventoryRepository.deleteByDateAfterAndRoom(today,room);
    }

    @Override
    public void deleteAllInventories(Room room) {
        log.info("Deleting all inventories ");
        inventoryRepository.deleteByRoom(room);

    }

    @Override
    public Page<PropertyResponseDto> searchProperties(PropertySearchRequestDto propertySearchRequestDto) {
        Pageable pageable = PageRequest.of(propertySearchRequestDto.getPage(), propertySearchRequestDto.getSize());

        long dateCount = ChronoUnit.DAYS.between(propertySearchRequestDto.getCheckInDate(), propertySearchRequestDto.getCheckOutDate());

        //business logic for 90 days
        Page<PropertyPriceDto> propertyPage =
                propertyMinPriceRepository.findPropertyWithAvailableInventory(propertySearchRequestDto.getCity(),
                        propertySearchRequestDto.getCheckInDate(), propertySearchRequestDto.getCheckOutDate(),
                        propertySearchRequestDto.getRoomsCount(), dateCount, pageable);

        // Convert every PropertyPriceDto in the page into a PropertyResponseDto
        return propertyPage.map((element) -> mapToPropertyResponseDto(element));
    }

    // Takes one "property + price" bundle, and turns it into the final API response shape
    private PropertyResponseDto mapToPropertyResponseDto(PropertyPriceDto propertyPriceDto) {

        // Auto-copy matching fields (name, city, amenities, etc.) from Property into PropertyResponseDto
        PropertyResponseDto dto = modelMapper.map(propertyPriceDto.getProperty(), PropertyResponseDto.class);

        // Property has no price field, so ModelMapper can't fill this in automatically —
        // manually attach the cheapest price we calculated in the search query
        dto.setPrice(propertyPriceDto.getPrice());

        return dto; // now dto has both property details AND the price
    }
}
