package com.sneha.airbnbAppC.controller;

import com.sneha.airbnbAppC.dto.booking.BookingRequestDto;
import com.sneha.airbnbAppC.dto.booking.BookingResponseDto;
import com.sneha.airbnbAppC.dto.booking.BookingStatusResponseDto;
import com.sneha.airbnbAppC.dto.guest.GuestRequestDto;
import com.sneha.airbnbAppC.dto.guest.GuestResponseDto;
import com.sneha.airbnbAppC.entity.Booking;
import com.sneha.airbnbAppC.service.BookingService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class PropertyBookingController {

    private final BookingService bookingService;


    @PostMapping
    public ResponseEntity<BookingResponseDto> initialiseBooking(@Valid @RequestBody BookingRequestDto bookingRequestDto){
        return ResponseEntity.ok(bookingService.initialiseBooking(bookingRequestDto));
    }

    @PostMapping("/{bookingId}/addGuests")
    public ResponseEntity<BookingResponseDto> addGuests(@PathVariable Long bookingId,
                                                         @Valid @RequestBody List<GuestRequestDto> guestRequestDtoList){
        return ResponseEntity.ok(bookingService.addGuests(bookingId,guestRequestDtoList));
    }

    @PostMapping("/{bookingId}/payments")
    public ResponseEntity<Map<String,String>> initiatePayments(@PathVariable Long bookingId){
        String sessionUrl = bookingService.initiatePayments(bookingId);
        return ResponseEntity.ok(Map.of("sessionUrl",sessionUrl));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookingId}/status")
    public ResponseEntity<BookingStatusResponseDto> getBookingStatus(@PathVariable Long bookingId) {
        return ResponseEntity.ok(new BookingStatusResponseDto(bookingService.getBookingStatus(bookingId)));
    }
}
