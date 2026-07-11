package com.sneha.airbnbAppC.dto.property;

import com.sneha.airbnbAppC.entity.PropertyContactInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class PropertyRequestDto {

    @NotBlank(message = "Property name is required")
    @Size(min = 3, max = 100, message = "Property name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "City is required")
    private String city;

    @NotNull(message = "Contact info is required")
    @Valid
    private PropertyContactInfo propertyContactInfo;


    private String[] photos;

    private String[] amenities;


}
