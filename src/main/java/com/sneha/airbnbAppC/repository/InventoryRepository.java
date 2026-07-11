package com.sneha.airbnbAppC.repository;

import com.sneha.airbnbAppC.entity.Inventory;
import com.sneha.airbnbAppC.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    void deleteByDateAfterAndRoom(LocalDate date, Room room);

    void deleteByRoom(Room room);
}