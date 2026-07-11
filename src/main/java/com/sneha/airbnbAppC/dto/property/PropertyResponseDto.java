package com.sneha.airbnbAppC.dto.property;

import com.sneha.airbnbAppC.entity.PropertyContactInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;


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

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
