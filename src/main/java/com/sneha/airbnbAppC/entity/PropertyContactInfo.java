package com.sneha.airbnbAppC.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class PropertyContactInfo {

  @NotBlank(message = "Address is required")
  private String address;

  @NotBlank(message = "Phone number is required")
  @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone number must be a valid 10-digit Indian mobile number")
  private String phoneNumber;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be a valid email address")
  private String email;


  private String location;
}