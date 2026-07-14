package com.sneha.airbnbAppC.dto.guest;

import com.sneha.airbnbAppC.entity.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GuestResponseDto {

    private Long id;
    private Long userId;
    private String name;
    private Gender gender;
    private Integer age;
    private LocalDateTime createdAt;
}
