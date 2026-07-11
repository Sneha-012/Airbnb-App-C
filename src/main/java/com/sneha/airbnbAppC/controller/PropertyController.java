package com.sneha.airbnbAppC.controller;

import com.sneha.airbnbAppC.dto.property.PropertyRequestDto;
import com.sneha.airbnbAppC.dto.property.PropertyResponseDto;
import com.sneha.airbnbAppC.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/properties")
@RequiredArgsConstructor
@Slf4j
public class PropertyController {

    private final PropertyService propertyService;


    @PostMapping
    public ResponseEntity<PropertyResponseDto> createProperty(@Valid @RequestBody PropertyRequestDto propertyRequestDto){
        log.info("Received request to creating new property: {}",propertyRequestDto.getName());
        PropertyResponseDto propertyResponseDto = propertyService.createProperty(propertyRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyResponseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponseDto> getPropertyById(@PathVariable Long id){
        log.info("Received request to fetch property with id: {}", id);
        PropertyResponseDto propertyResponseDto = propertyService.getPropertyById(id);
        return ResponseEntity.ok(propertyResponseDto);
    }

    @GetMapping
    public ResponseEntity<List<PropertyResponseDto>> getAllProperties(){
        log.info("Received request to fetch all properties");
        return ResponseEntity.ok(propertyService.getAllProperties());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponseDto> updatePropertyById(@PathVariable Long id, @Valid @RequestBody PropertyRequestDto propertyRequestDto){
        log.info("Received request to update property: {}",propertyRequestDto.getName());
        PropertyResponseDto propertyResponseDto = propertyService.updatePropertyById(id, propertyRequestDto);
        return ResponseEntity.ok(propertyResponseDto);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deletePropertyById(@PathVariable Long id){
        log.info("Received request to delete property with id: {}", id);
        propertyService.deletePropertyById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> activePropertyById(@PathVariable Long id){
        log.info("Received request to active property with id: {}", id);
        propertyService.activeProperty(id);
        return ResponseEntity.noContent().build();
    }


}
