package com.sneha.airbnbAppC.repository;

import com.sneha.airbnbAppC.entity.Inventory;
import com.sneha.airbnbAppC.entity.Property;
import com.sneha.airbnbAppC.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    void deleteByDateAfterAndRoom(LocalDate date, Room room);

    void deleteByRoom(Room room);


    @Query("""
            SELECT DISTINCT i.property
            FROM Inventory i
            WHERE i.city = :city
                AND i.date >= :checkInDate
                AND i.date < :checkOutDate
                AND i.closed = FALSE
                AND (i.totalCount - i.bookedCount >= :roomsCount)
            GROUP BY i.property, i.room
            HAVING COUNT(i.date) = :dateCount
            """)
    Page<Property> findPropertyWithAvailableInventory(
      @Param("city") String city,
      @Param("checkInDate") LocalDate checkInDate,
      @Param("checkOutDate") LocalDate checkOutDate,
      @Param("roomsCount") Integer roomsCount,
      @Param("dateCount") Long dateCount,
      Pageable pageable
    );
}