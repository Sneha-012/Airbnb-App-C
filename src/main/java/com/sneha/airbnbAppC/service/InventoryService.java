package com.sneha.airbnbAppC.service;

import com.sneha.airbnbAppC.dto.inventory.InventoryResponseDto;
import com.sneha.airbnbAppC.dto.property.PropertyResponseDto;
import com.sneha.airbnbAppC.dto.property.PropertySearchRequestDto;
import com.sneha.airbnbAppC.entity.Room;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteFutureInventories(Room room);

    void deleteAllInventories(Room room);

    Page<PropertyResponseDto> searchProperties(@Valid PropertySearchRequestDto propertySearchRequestDto);

    List<InventoryResponseDto> getAllInventoryByRoomId(Long roomId);
}
