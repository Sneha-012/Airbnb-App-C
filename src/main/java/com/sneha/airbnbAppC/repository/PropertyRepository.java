package com.sneha.airbnbAppC.repository;

import com.sneha.airbnbAppC.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {

}