package com.sneha.airbnbAppC.controller;

import com.sneha.airbnbAppC.dto.room.RoomRequestDto;
import com.sneha.airbnbAppC.dto.room.RoomResponseDto;
import com.sneha.airbnbAppC.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/{propertyId}")
    public ResponseEntity<RoomResponseDto> createRoom(@PathVariable Long propertyId, @Valid @RequestBody RoomRequestDto roomRequestDto){
        log.info("Received request to creating new room: {}",roomRequestDto.getType());
        RoomResponseDto roomResponseDto = roomService.createRoom(propertyId,roomRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(roomResponseDto);

    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<RoomResponseDto>> getAllRoomInProperty(@PathVariable Long propertyId){
        log.info("Received request to fetch room with property id: {}", propertyId);
        return ResponseEntity.ok(roomService.getAllRoomInProperty(propertyId));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponseDto> getRoomById(@PathVariable Long roomId){
        log.info("Received request to fetch room with id: {}", roomId);
        RoomResponseDto roomResponseDto = roomService.getRoomById(roomId);
        return ResponseEntity.ok(roomResponseDto);
    }

    @PutMapping("/property/{propertyId}/{roomId}")
    public ResponseEntity<RoomResponseDto> updateRoomById(@PathVariable Long propertyId,@PathVariable Long roomId, @Valid @RequestBody RoomRequestDto roomRequestDto){
        log.info("Received request to update room with id: {}",roomId);
        RoomResponseDto roomResponseDto = roomService.updateRoomById(propertyId,roomId,roomRequestDto);
        return ResponseEntity.ok(roomResponseDto);

    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Boolean> deleteRoomById(@PathVariable Long roomId){
        log.info("Received request to delete room with id: {}",roomId);
        roomService.deleteRoomById(roomId);
        return ResponseEntity.noContent().build();
    }
}
