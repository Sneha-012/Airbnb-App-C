package com.sneha.airbnbAppC.service;

import com.sneha.airbnbAppC.dto.booking.BookingRequestDto;
import com.sneha.airbnbAppC.dto.booking.BookingResponseDto;
import com.sneha.airbnbAppC.dto.guest.GuestRequestDto;
import com.sneha.airbnbAppC.dto.guest.GuestResponseDto;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;


public interface BookingService {

    BookingResponseDto initialiseBooking(BookingRequestDto bookingRequestDto);

    BookingResponseDto addGuests(Long bookingId, List<GuestRequestDto> guestRequestDtoList);
}
