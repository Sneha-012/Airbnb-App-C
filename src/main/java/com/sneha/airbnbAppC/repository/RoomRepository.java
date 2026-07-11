package com.sneha.airbnbAppC.repository;

import com.sneha.airbnbAppC.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}