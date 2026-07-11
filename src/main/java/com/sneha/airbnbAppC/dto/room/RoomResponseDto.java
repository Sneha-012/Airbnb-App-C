package com.sneha.airbnbAppC.dto.room;

import com.sneha.airbnbAppC.entity.Property;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponseDto {


    private Long id;

    private Long propertyId;

    private String type;

    private BigDecimal basePrice;

    private String[] photos;

    private String[] amenities;

    private Integer totalCount;

    private Integer capacity;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
