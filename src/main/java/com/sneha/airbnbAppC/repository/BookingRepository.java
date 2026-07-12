package com.sneha.airbnbAppC.repository;

import com.sneha.airbnbAppC.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}