package com.sneha.airbnbAppC.service;

import com.sneha.airbnbAppC.dto.room.RoomRequestDto;
import com.sneha.airbnbAppC.dto.room.RoomResponseDto;

import java.util.List;

public interface RoomService {

    RoomResponseDto createRoom(Long propertyId , RoomRequestDto roomRequestDto);

    List<RoomResponseDto> getAllRoomInProperty(Long propertyId);

    RoomResponseDto getRoomById(Long roomId);

    Boolean deleteRoomById(Long roomId);

    RoomResponseDto updateRoomById(Long propertyId, Long roomId, RoomRequestDto roomRequestDto);
}
