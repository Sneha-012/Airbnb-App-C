package com.sneha.airbnbAppC.dto.booking;

import com.sneha.airbnbAppC.dto.guest.GuestRequestDto;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {

    @NotNull(message = "Property id is required")
    private Long propertyId;

    @NotNull(message = "Room id is required")
    private Long roomId;

    @NotNull(message = "Check-in date is required")
    @Future(message = "Check-in date must be in the future")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;

    @NotNull(message = "Rooms count is required")
    @Min(value = 1, message = "At least 1 room must be booked")
    private Integer roomsCount;

    //@NotEmpty(message = "At least one guest is required")
    private Set<GuestRequestDto> guests;
}
