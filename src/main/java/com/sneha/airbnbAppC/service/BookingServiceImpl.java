package com.sneha.airbnbAppC.service;

import com.sneha.airbnbAppC.dto.booking.BookingRequestDto;
import com.sneha.airbnbAppC.dto.booking.BookingResponseDto;
import com.sneha.airbnbAppC.dto.guest.GuestRequestDto;
import com.sneha.airbnbAppC.dto.guest.GuestResponseDto;
import com.sneha.airbnbAppC.entity.*;
import com.sneha.airbnbAppC.entity.enums.BookingStatus;
import com.sneha.airbnbAppC.exception.ResourceNotFoundException;
import com.sneha.airbnbAppC.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{
    private final GuestRepository guestRepository;

    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final BookingRepository bookingRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public BookingResponseDto initialiseBooking(BookingRequestDto bookingRequestDto) {

        log.info("Initiating the Booking for property id: {},room id: {}, date {} - {} ",
                bookingRequestDto.getPropertyId(), bookingRequestDto.getRoomId(),
                bookingRequestDto.getCheckInDate(), bookingRequestDto.getCheckOutDate());

        Property property = propertyRepository.findById(bookingRequestDto.getPropertyId())
                .orElseThrow(()->{
                    log.warn("Cannot initiate booking - Property doesn't exist with Id: {}",bookingRequestDto.getPropertyId());
                    return new ResourceNotFoundException("Property not found with id: "+bookingRequestDto.getPropertyId());
                });

        if (!property.getActive()) {
            log.warn("Cannot initiate booking - Property is not active, id: {}", bookingRequestDto.getPropertyId());
            throw new IllegalStateException("Property is not currently available for booking");
        }

        Room room = roomRepository.findById(bookingRequestDto.getRoomId())
                .orElseThrow(()->{
                    log.warn("Cannot initiate booking - Room doesnot exists by Id: {}",bookingRequestDto.getRoomId());
                    return new ResourceNotFoundException("Room doesnot exists by Id: "+bookingRequestDto.getRoomId());
                });

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(
                                                            bookingRequestDto.getRoomId(),
                                                            bookingRequestDto.getCheckInDate(),
                                                            bookingRequestDto.getCheckOutDate(),
                                                            bookingRequestDto.getRoomsCount());

        long dayCount = ChronoUnit.DAYS.between(bookingRequestDto.getCheckInDate(),
                                                bookingRequestDto.getCheckOutDate());

        if (inventoryList.size() != dayCount ){
            throw new IllegalStateException("Room is not available anymore of id : " +bookingRequestDto.getRoomId());
        }

        for(Inventory inventory: inventoryList){
            inventory.setReservedCount(inventory.getReservedCount() + bookingRequestDto.getRoomsCount());
        }
        inventoryRepository.saveAll(inventoryList);

        //Create the Booking

        User user = new User();
        user.setId(1L); //get dummy user

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .property(property)
                .room(room)
                .checkInDate(bookingRequestDto.getCheckInDate())
                .checkOutDate(bookingRequestDto.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequestDto.getRoomsCount())
                .amount(BigDecimal.TEN)
                .build();

        booking = bookingRepository.save(booking);
        return modelMapper.map(booking,BookingResponseDto.class);
    }

    @Override
    @Transactional
    public BookingResponseDto addGuests(Long bookingId, List<GuestRequestDto> guestRequestDtoList) {

        log.info("Adding the guest with booking id: {} ",bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()->{
                    log.warn("Cannot add guests - Booking doesn't exist with Id: {}",bookingId);
                    return new ResourceNotFoundException("Booking not found with id: "+bookingId);
                });

        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has expired");
        }

        if(booking.getBookingStatus() != BookingStatus.RESERVED){
            throw new IllegalStateException("Booking is not under Reserved state");
        }

        for(GuestRequestDto guestRequestDto: guestRequestDtoList ) {
            Guest guest = modelMapper.map(guestRequestDto, Guest.class);
            guest.setUser(getCurrentUser());
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUEST_ADDED);
        booking = bookingRepository.save(booking);

        log.info("Added {} guest(s) successfully to booking id: {}", guestRequestDtoList.size(), bookingId);

        return modelMapper.map(booking,BookingResponseDto.class);
    }

    public Boolean hasBookingExpired(Booking booking){
        return (booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now()));

    }

    public User getCurrentUser(){
        User user = new User();
        user.setId(1L);
        return user;
    }


}
