package com.sneha.airbnbAppC.security;


import com.sneha.airbnbAppC.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JWTService {

    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    private static final long ACCESS_TOKEN_EXPIRY_MS = 1000L * 60 * 10;              // 10 minutes
    private static final long REFRESH_TOKEN_EXPIRY_MS = 1000L * 60 * 60 * 24 * 30 * 6; // ~6 months

    // JWT libraries need a proper SecretKey object, not a plain string — this converts it.
    private SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    // Creates a short-lived token used to authenticate normal requests.
    // Kept short (10 min) on purpose — if stolen, the damage window is small.
    public String generateAccessToken(User user){
        return Jwts.builder()
                .subject(user.getId().toString())       // whose token this is
                .claim("email", user.getEmail())         // extra info stamped in, avoids extra DB lookups
                .claim("roles", user.getRoles())
                .issuedAt(new Date())                    // when it was created
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY_MS))
                .signWith(getSecretKey())                // stamps it so it can't be forged
                .compact();                              // turns it into the final token string
    }

    // Creates a long-lived token whose only job is: "let the user get a new
    // access token later without logging in again with their password."
    public String generateRefreshToken(User user){
        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY_MS))
                .signWith(getSecretKey())
                .compact();
    }

    // Verifies a token is genuine (using our secret key) and pulls the user ID out of it.
    // Throws automatically if the token was tampered with or wasn't signed by us.
    public Long getUserIdFromToken(String token){
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }
}
