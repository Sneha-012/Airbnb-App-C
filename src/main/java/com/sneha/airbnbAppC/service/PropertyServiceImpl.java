package com.sneha.airbnbAppC.service;

import com.sneha.airbnbAppC.dto.property.PropertyInfoDto;
import com.sneha.airbnbAppC.dto.property.PropertyRequestDto;
import com.sneha.airbnbAppC.dto.room.RoomResponseDto;
import com.sneha.airbnbAppC.entity.Property;
import com.sneha.airbnbAppC.dto.property.PropertyResponseDto;
import com.sneha.airbnbAppC.entity.Room;
import com.sneha.airbnbAppC.exception.ResourceNotFoundException;
import com.sneha.airbnbAppC.repository.PropertyRepository;
import com.sneha.airbnbAppC.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor //
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;
//    public PropertyServiceImpl(PropertyRepository propertyRepository) {
//        this.propertyRepository = propertyRepository;
//    }

    @Override
    public PropertyResponseDto createProperty(PropertyRequestDto propertyRequestDto) {
        log.info("Creating new Property with name: {}", propertyRequestDto.getName());

        Property property = modelMapper.map(propertyRequestDto, Property.class);
        property.setActive(false);

        Property savedProperty = propertyRepository.save(property);
        log.info("Property created successfully with id: {}",savedProperty.getId());

        return modelMapper.map(savedProperty , PropertyResponseDto.class);
    }

    @Override
    public PropertyResponseDto getPropertyById(Long id) {
        log.debug("Fetch the property with id: {}", id);
        Property property = propertyRepository.findById(id)
                .orElseThrow(()-> {
                    log.warn("Property not found with id: {}", id);
                    return new ResourceNotFoundException("Property not found with id: " + id);
                });


        return modelMapper.map(property,PropertyResponseDto.class);
    }

    @Override
    public List<PropertyResponseDto> getAllProperties() {
        log.debug("Fetch all the properties");
        List<PropertyResponseDto> propertyResponseDtoList = propertyRepository.findAll()
                .stream()
                .map(property -> modelMapper.map(property,PropertyResponseDto.class))
                .collect(Collectors.toList());
        log.debug("Fetched {} properties", propertyResponseDtoList.size());
        return propertyResponseDtoList;
    }

    @Override
    public PropertyResponseDto updatePropertyById(Long id, PropertyRequestDto propertyRequestDto) {
        log.info("Updating property with id: {}", id);

        Property existingProperty = propertyRepository.findById(id)
                .orElseThrow(()->{
                    log.warn("Cannot update — property not found with id: {}", id);
                    return new ResourceNotFoundException("Property not found with id: "+id);
                });
        existingProperty.setName(propertyRequestDto.getName());
        existingProperty.setCity(propertyRequestDto.getCity());
        existingProperty.setPropertyContactInfo(propertyRequestDto.getPropertyContactInfo());
        existingProperty.setAmenities(propertyRequestDto.getAmenities());
        existingProperty.setPhotos(propertyRequestDto.getPhotos());

        Property updatedProperty = propertyRepository.save(existingProperty);
        log.info("Property updated successfully with id: {}", updatedProperty.getId());

        return modelMapper.map(updatedProperty,PropertyResponseDto.class);
    }

    @Override
    @Transactional
    public Boolean deletePropertyById(Long id) {
        log.info("Deleting property with id: {}", id);

        Property property = propertyRepository.findById(id)
                .orElseThrow(()->{
                    log.warn("Cannot update — property not found with id: {}", id);
                    return new ResourceNotFoundException("Property not found with id: "+id);
                });

        //delete future inventories for this hotel
        for(Room room: property.getRooms()){
            inventoryService.deleteAllInventories(room);
            roomRepository.delete(room);
        }

        propertyRepository.deleteById(id);

        return true;
    }

    @Override
    @Transactional
    public void activeProperty(Long id) {

        log.info("Activating property with id: {}", id);

        Property property = propertyRepository.findById(id)
                .orElseThrow(()->{
                    log.warn("Cannot update — property not found with id: {}", id);
                    return new ResourceNotFoundException("Property not found with id: "+id);
                });
        // To check whether our property is already active or not
        if (property.getActive()) {
            log.warn("Property is already active, id: {}", id);
            throw new IllegalStateException("Property is already active with id: " + id);
        }

        property.setActive(true);
        propertyRepository.save(property);

        //Assuming do it once
        for(Room room: property.getRooms()){
            inventoryService.initializeRoomForAYear(room);
        }
        log.info("Property activated successfully with id: {}", id);
    }

    @Override
    @Transactional
    public void deactivateProperty(Long id) {

        log.info("Deactivating property with id: {}", id);

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot deactivate — property not found with id: {}", id);
                    return new ResourceNotFoundException("Property not found with id: " + id);
                });

        property.setActive(false);
        propertyRepository.save(property);

        for (Room room : property.getRooms()) {
            inventoryService.deleteFutureInventories(room);
        }

        log.info("Property deactivated successfully with id: {}", id);
    }


    @Override
    public PropertyInfoDto getPropertyInfo(Long propertyId) {
        log.debug("Fetch the property with id: {}", propertyId);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(()-> {
                    log.warn("Property not found with id: {}", propertyId);
                    return new ResourceNotFoundException("Property not found with id: " + propertyId);
                });

        PropertyResponseDto propertyResponseDto = modelMapper.map(property,PropertyResponseDto.class);

        List<RoomResponseDto> roomResponseDtoList = property.getRooms()
                .stream().map(roomList ->modelMapper.map(roomList,RoomResponseDto.class))
                .collect(Collectors.toList());

        return new PropertyInfoDto(propertyResponseDto,roomResponseDtoList) ;
    }




}
