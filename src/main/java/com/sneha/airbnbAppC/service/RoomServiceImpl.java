package com.sneha.airbnbAppC.service;

import com.sneha.airbnbAppC.dto.room.RoomRequestDto;
import com.sneha.airbnbAppC.dto.room.RoomResponseDto;
import com.sneha.airbnbAppC.entity.Property;
import com.sneha.airbnbAppC.entity.Room;
import com.sneha.airbnbAppC.exception.ResourceNotFoundException;
import com.sneha.airbnbAppC.repository.PropertyRepository;
import com.sneha.airbnbAppC.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final PropertyRepository propertyRepository;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public RoomResponseDto createRoom(Long propertyId, RoomRequestDto roomRequestDto) {
       log.info("Creating a room of type: {} for property Id: {} ",roomRequestDto.getType(),propertyId);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(()->{
                    log.warn("Cannot create a room - Property doesn't exist with Id: {}",propertyId);
                    return new ResourceNotFoundException("Property not found with id: "+propertyId);
                });
        Room room = modelMapper.map(roomRequestDto, Room.class);
        room.setProperty(property);
        Room savedRoom =  roomRepository.save(room);

        if(property.getActive()){
            inventoryService.initializeRoomForAYear(room);
        }
        log.info("Room created successfully with id: {}", savedRoom.getId());
        return modelMapper.map(savedRoom,RoomResponseDto.class);
    }

    @Override
    public List <RoomResponseDto> getAllRoomInProperty(Long propertyId) {
        log.info("Fetching  room for property Id: {} ",propertyId);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(()->{
                    log.warn("Cannot fetch room - Property doesn't exist with Id: {}",propertyId);
                    return new ResourceNotFoundException("Property not found with id: "+propertyId);
                });

        return property.getRooms()
                .stream()
                .map(roomList ->modelMapper.map(roomList,RoomResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomResponseDto getRoomById(Long roomId) {
        log.debug("Fetch the room by Id: {}",roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()->{
                    log.warn("Room doesnot exists by Id: {}",roomId);
                    return new ResourceNotFoundException("Room doesnot exists by Id: "+roomId);
                });
        return modelMapper.map(room,RoomResponseDto.class);
    }

    @Override
    @Transactional
    public Boolean deleteRoomById(Long roomId) {
        log.debug("Deleting the room by Id: {}",roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()->{
                    log.warn("Cannot delete the room as it  doesnot exists by Id: {}",roomId);
                    return new ResourceNotFoundException("Room doesnot exists by Id: "+roomId);
                });
        //deleting future inventories of room
        inventoryService.deleteAllInventories(room);
        roomRepository.deleteById(roomId);
        return true;
    }

    @Override
    public RoomResponseDto updateRoomById(Long propertyId, Long roomId, RoomRequestDto roomRequestDto) {

        log.info("Updating the room of Id {} for property Id: {} ",roomId,propertyId);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(()->{
                    log.warn("Cannot update a room - Property doesn't exist with Id: {}",propertyId);
                    return new ResourceNotFoundException("Property not found with id: "+propertyId);
                });

        Room existingRoom = roomRepository.findById(roomId)
                .orElseThrow(()->{
                    log.warn("Cannot update a room - Room doesn't exist with Id: {}",roomId);
                    return new ResourceNotFoundException("Room doesnot exists by Id: "+roomId);
                });
        existingRoom.setType(roomRequestDto.getType());
        existingRoom.setCapacity(roomRequestDto.getCapacity());
        existingRoom.setAmenities(roomRequestDto.getAmenities());
        existingRoom.setPhotos(roomRequestDto.getPhotos());
        existingRoom.setTotalCount(roomRequestDto.getTotalCount());
        existingRoom.setBasePrice(roomRequestDto.getBasePrice());

        Room updatedRoom = roomRepository.save(existingRoom);
        log.info("Room updated successfully with id: {}", updatedRoom.getId());

        return modelMapper.map(updatedRoom,RoomResponseDto.class);
    }
}
