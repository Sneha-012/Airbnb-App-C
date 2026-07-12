package com.sneha.airbnbAppC.service;

import com.sneha.airbnbAppC.dto.property.PropertyResponseDto;
import com.sneha.airbnbAppC.dto.property.PropertySearchRequestDto;
import com.sneha.airbnbAppC.entity.Inventory;
import com.sneha.airbnbAppC.entity.Property;
import com.sneha.airbnbAppC.entity.Room;
import com.sneha.airbnbAppC.repository.InventoryRepository;
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

        long dateCount = ChronoUnit.DAYS.between(propertySearchRequestDto.getCheckInDate(),propertySearchRequestDto.getCheckOutDate());

        Page<Property> propertyPage = inventoryRepository.findPropertyWithAvailableInventory(propertySearchRequestDto.getCity(),
                propertySearchRequestDto.getCheckInDate(),propertySearchRequestDto.getCheckOutDate(),
                propertySearchRequestDto.getRoomsCount(),dateCount,pageable);



        return propertyPage.map((element)->modelMapper.map((element),PropertyResponseDto.class));
        //This says: "for every Property inside this page, convert just that one object into a PropertyResponseDto, and give me back a Page<PropertyResponseDto> with the same pagination shape."
    }
}
