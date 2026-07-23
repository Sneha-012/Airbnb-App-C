package com.sneha.airbnbAppC.security;

import com.sneha.airbnbAppC.entity.User;
import com.sneha.airbnbAppC.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class JWTAuthFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserService userService;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // Look for a header like: Authorization: Bearer <token>
            final String requestTokenHeader = request.getHeader("Authorization");

            // No token, or wrong format -> just let the request continue as "not logged in"
            // (some endpoints, like public property search, don't require login anyway)
            if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer")) {
                filterChain.doFilter(request, response);
                return;
            }

            // Strip off "Bearer " to get just the raw token string
            String token = requestTokenHeader.split("Bearer ")[1];

            // Verify the token is genuine and extract which user it belongs to.
            // Throws JwtException automatically if invalid/expired (caught below).
            Long userId = jwtService.getUserIdFromToken(token);

            // Only proceed if we got a valid user id, and this request isn't
            // already marked as authenticated by something else.
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userService.getUserById(userId);

                // This is Spring Security's official "who is logged in" object.
                // No password needed here since JWT already proved identity.
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                // Attach extra request metadata (IP, session info) - mostly for logging/auditing
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // THE key line: tells Spring Security "treat this request as logged in as this user"
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

            // Always pass the request onward — every filter must do this unless deliberately blocking it
            filterChain.doFilter(request, response);

        } catch (JwtException ex) {
            // Bad/expired token -> hand off to Spring's exception handling
            // instead of letting a raw stack trace reach the user
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }
}
