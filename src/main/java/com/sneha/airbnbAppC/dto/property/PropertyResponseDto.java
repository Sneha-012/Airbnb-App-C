package com.sneha.airbnbAppC.dto.property;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sneha.airbnbAppC.entity.PropertyContactInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyResponseDto {

    private Long id;

    private String name;

    private String city;

    private PropertyContactInfo propertyContactInfo;

    private String[] photos;

    private String[] amenities;

    @JsonInclude(NON_NULL)
    private BigDecimal price; // populated only during search (with dates); null otherwise

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
