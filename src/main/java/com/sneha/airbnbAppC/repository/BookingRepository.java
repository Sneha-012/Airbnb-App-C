package com.sneha.airbnbAppC.repository;

import com.sneha.airbnbAppC.entity.Booking;
import com.sneha.airbnbAppC.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByPaymentSessionId(String sessionId);

    List<Booking> findByProperty(Property property);
}