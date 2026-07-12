package com.sneha.airbnbAppC.service;

import com.sneha.airbnbAppC.dto.property.PropertyInfoDto;
import com.sneha.airbnbAppC.dto.property.PropertyRequestDto;
import com.sneha.airbnbAppC.dto.property.PropertyResponseDto;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface PropertyService {

    PropertyResponseDto createProperty(PropertyRequestDto propertyRequestDto);

    PropertyResponseDto getPropertyById(Long id);

    List<PropertyResponseDto> getAllProperties();

    PropertyResponseDto updatePropertyById(Long id, PropertyRequestDto propertyRequestDto);

    Boolean deletePropertyById(Long id);

    void activeProperty(Long id);

    PropertyInfoDto getPropertyInfo(Long propertyId);
}
