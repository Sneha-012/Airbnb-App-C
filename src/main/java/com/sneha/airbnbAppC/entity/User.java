package com.sneha.airbnbAppC.entity;

import com.sneha.airbnbAppC.entity.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Setter
@Getter
@Table(name = "app_user")//because database already has user table
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    // Spring Security calls this automatically to find out what this user is allowed to do.
    // It doesn't understand our own Role enum directly, so we convert each Role
    // into a SimpleGrantedAuthority (the format Spring Security expects).
    // We prefix "ROLE_" because Spring Security's built-in checks (like hasRole("ADMIN"))
    // automatically look for "ROLE_" + name — without this prefix, those checks silently fail.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return email;
    }
}
