package com.sneha.airbnbAppC.repository;

import com.sneha.airbnbAppC.entity.Inventory;
import com.sneha.airbnbAppC.entity.Property;
import com.sneha.airbnbAppC.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;
import java.util.List;

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
                AND (i.totalCount - i.bookedCount - i.reservedCount >= :roomsCount)
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

    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.room.id = :roomId
            AND i.date >= :checkInDate
            AND i.date < :checkOutDate
            AND i.closed = FALSE
            AND (i.totalCount - i.bookedCount - i.reservedCount >= :roomsCount)
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findAndLockAvailableInventory(
         // @Param("propertyId") Long propertyId,
          @Param("roomId") Long roomId,
          @Param("checkInDate") LocalDate checkInDate,
          @Param("checkOutDate") LocalDate checkOutDate,
          @Param("roomsCount") Integer roomsCount
    );
}