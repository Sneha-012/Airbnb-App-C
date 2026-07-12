package com.sneha.airbnbAppC.controller;

import com.sneha.airbnbAppC.dto.property.PropertyInfoDto;
import com.sneha.airbnbAppC.dto.property.PropertyResponseDto;
import com.sneha.airbnbAppC.dto.property.PropertySearchRequestDto;
import com.sneha.airbnbAppC.service.InventoryService;
import com.sneha.airbnbAppC.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/properties")
@RequiredArgsConstructor
public class PropertyBrowseController {

    private final InventoryService inventoryService;
    private final PropertyService propertyService;


    @GetMapping("/search")
    public ResponseEntity<Page<PropertyResponseDto>> searchProperties(@Valid @RequestBody PropertySearchRequestDto propertySearchRequestDto){
        Page<PropertyResponseDto> page = inventoryService.searchProperties(propertySearchRequestDto);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<PropertyInfoDto> getPropertyInfo(@PathVariable Long propertyId){
        return ResponseEntity.ok(propertyService.getPropertyInfo(propertyId));
    }
}
