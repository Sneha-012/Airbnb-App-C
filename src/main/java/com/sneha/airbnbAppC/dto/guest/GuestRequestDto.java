package com.sneha.airbnbAppC.dto.guest;

import com.sneha.airbnbAppC.entity.enums.Gender;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GuestRequestDto {

    @NotBlank(message = "Guest name is required")
    private String name;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @Min(value = 0, message = "Age must be a positive number")
    private Integer age;
}
