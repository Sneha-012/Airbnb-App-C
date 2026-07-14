package com.sneha.airbnbAppC.repository;

import com.sneha.airbnbAppC.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {

}