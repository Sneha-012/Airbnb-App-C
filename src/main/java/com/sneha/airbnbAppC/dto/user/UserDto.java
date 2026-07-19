package com.sneha.airbnbAppC.dto.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
public class UserDto {
    private String email;
    private String name;
    private Long id;
}
