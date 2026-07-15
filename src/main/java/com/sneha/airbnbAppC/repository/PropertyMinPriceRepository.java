package com.sneha.airbnbAppC.repository;

import com.sneha.airbnbAppC.dto.property.PropertyPriceDto;
import com.sneha.airbnbAppC.entity.PropertyMinPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface PropertyMinPriceRepository extends JpaRepository<PropertyMinPrice, Long> {


    @Query("""
            SELECT com.sneha.airbnbAppC.dto.property.PropertyPriceDto(i.property ,AVG(i.price))
            FROM PropertyMinPrice i
            WHERE i.property.city = :city
                AND i.date >= :checkInDate
                AND i.date < :checkOutDate
                AND i.property.active = TRUE
            GROUP BY i.property
            HAVING COUNT(i.date) = :dateCount
            """)
    Page<PropertyPriceDto> findPropertyWithAvailableInventory(
            @Param("city") String city,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );

}