package com.sneha.airbnbAppC.dto.booking;

import com.sneha.airbnbAppC.dto.guest.GuestResponseDto;
import com.sneha.airbnbAppC.dto.room.RoomResponseDto;
import com.sneha.airbnbAppC.entity.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {
    private Long id;
    //private RoomResponseDto room;
    private Integer roomsCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BookingStatus bookingStatus;
    private Set<GuestResponseDto> guests;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
