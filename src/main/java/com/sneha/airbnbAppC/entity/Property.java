package com.sneha.airbnbAppC.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
@Table
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String city;

    @Embedded
    private PropertyContactInfo propertyContactInfo;

    @Column(columnDefinition = "TEXT[]") //will contain url
    private String[] photos;

    @Column(columnDefinition = "TEXT[]") //will contain Wi-Fi, ac like
    private String[] amenities;

    @Column(nullable = false)
    private Boolean active;

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY)
    private List<Room> rooms;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
