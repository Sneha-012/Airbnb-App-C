package com.sneha.airbnbAppC.service;

import com.sneha.airbnbAppC.dto.booking.BookingRequestDto;
import com.sneha.airbnbAppC.dto.booking.BookingResponseDto;
import com.sneha.airbnbAppC.dto.guest.GuestRequestDto;
import com.sneha.airbnbAppC.dto.guest.GuestResponseDto;
import com.sneha.airbnbAppC.entity.enums.BookingStatus;
import com.stripe.model.Event;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;


public interface BookingService {

    BookingResponseDto initialiseBooking(BookingRequestDto bookingRequestDto);

    BookingResponseDto addGuests(Long bookingId, List<GuestRequestDto> guestRequestDtoList);


    String initiatePayments(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    BookingStatus getBookingStatus(Long bookingId);

    List<BookingResponseDto> getAllBookingsByPropertyId(Long propertyId);
}
