package com.sneha.airbnbAppC.service;

import com.sneha.airbnbAppC.dto.booking.BookingRequestDto;
import com.sneha.airbnbAppC.dto.booking.BookingResponseDto;
import com.sneha.airbnbAppC.dto.guest.GuestRequestDto;
import com.sneha.airbnbAppC.entity.*;
import com.sneha.airbnbAppC.entity.enums.BookingStatus;
import com.sneha.airbnbAppC.exception.ResourceNotFoundException;
import com.sneha.airbnbAppC.exception.UnAuthorisedException;
import com.sneha.airbnbAppC.pricingStrategy.PricingService;
import com.sneha.airbnbAppC.repository.*;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

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
    private final PricingService pricingService;
    private final CheckOutService checkOutService;


    @Value("${frontend.url}")
    private String frontendUrl;

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
        //changing this for loop to some query in inventory repository init

        for(Inventory inventory: inventoryList){
            inventory.setReservedCount(inventory.getReservedCount() + bookingRequestDto.getRoomsCount());
        }
        inventoryRepository.saveAll(inventoryList);

        //Create the Booking
//        User user = new User();
//        user.setId(1L); //get dummy user
        //User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequestDto.getRoomsCount()));

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .property(property)
                .room(room)
                .checkInDate(bookingRequestDto.getCheckInDate())
                .checkOutDate(bookingRequestDto.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequestDto.getRoomsCount())
                .amount(totalPrice)
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

    @Override
    public String initiatePayments(Long bookingId) {
        Booking booking  = bookingRepository.findById(bookingId)
                .orElseThrow(()->{
                    log.warn("Cannot initiate payment - Booking doesn't exist with Id: {}",bookingId);
                    return new ResourceNotFoundException("Booking not found with id: "+bookingId);
                });

        User user = getCurrentUser();
        if(!user.getId().equals(booking.getUser().getId())){
            throw new UnAuthorisedException("Booking doesnot belong to current user with id: "+ booking.getUser().getId());
        }

        if(hasBookingExpired(booking)){
            throw new IllegalArgumentException("Booking has already expired with id : "+ bookingId);
        }

        String sessionUrl = checkOutService.getCheckOutSession(booking,
                frontendUrl+"/payment/success",frontendUrl+"/payment/failure");

        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);
        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) return;

            String sessionId = session.getId();
            Booking booking =
                    bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(() ->
                            new ResourceNotFoundException("Booking not found for session ID: "+sessionId));

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());

            log.info("Successfully confirmed the booking for Booking ID: {}", booking.getId());
        } else {
            log.warn("Unhandled event type: {}", event.getType());
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {

        Booking booking  = bookingRepository.findById(bookingId)
                .orElseThrow(()->{
                    log.warn("Cannot cancel booking - Booking doesn't exist with Id: {}",bookingId);
                    return new ResourceNotFoundException("Booking not found with id: "+bookingId);
                });

        User user = getCurrentUser();
        if(!user.getId().equals(booking.getUser().getId())){
            throw new UnAuthorisedException("Booking doesnot belong to current user with id: "+ booking.getUser().getId());
        }

        if(booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed bookings can be cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getRoomsCount());

        inventoryRepository.cancelBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getRoomsCount());

        // handle the refund
        try {
                Session session = Session.retrieve(booking.getPaymentSessionId());
                RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();
                Refund.create(refundParams);
            }catch (StripeException e) {
                throw new RuntimeException(e);
        }
    }

    @Override
    public BookingStatus getBookingStatus(Long bookingId) {
        Booking booking  = bookingRepository.findById(bookingId)
                .orElseThrow(()->{
                    log.warn("Cannot fetch booking status - Booking doesn't exist with Id: {}",bookingId);
                    return new ResourceNotFoundException("Booking not found with id: "+bookingId);
                });
        User user = getCurrentUser();
        if(!user.getId().equals(booking.getUser().getId())){
            throw new UnAuthorisedException("Booking doesn't belong to current user with id: "+ booking.getUser().getId());
        }
        return booking.getBookingStatus();
    }

    @Override
    public List<BookingResponseDto> getAllBookingsByPropertyId(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(()->{
                    log.warn("Cannot initiate booking - Property doesn't exist with Id: {}",propertyId);
                    return new ResourceNotFoundException("Property not found with id: "+ propertyId);
                });
        User user = getCurrentUser();

        log.info("Getting all booking for the property with ID: {}", propertyId);
        if(!user.equals(property.getOwner())) throw new AccessDeniedException("You are not the owner of property with id: "+ propertyId);

        List<Booking> bookings = bookingRepository.findByProperty(property);

        return bookings.stream()
                .map((element) -> modelMapper.map(element, BookingResponseDto.class))
                .collect(Collectors.toList());
    }


    public Boolean hasBookingExpired(Booking booking){
        return (booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now()));

    }

    public User getCurrentUser(){

        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    }


}
