package com.example.lockly.security;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message,
                              MessageChannel channel) {


        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        log.info("Command={}", accessor.getCommand());
        log.info("Headers={}", accessor.toNativeHeaderMap());

        // Không phải frame STOMP (hiếm gặp)
        if (accessor.getCommand() == null) {
            return message;
        }

        log.debug("=== STOMP INTERCEPTOR START ===");
        log.debug("STOMP Command: {}", accessor.getCommand());
        log.debug("Session Id: {}", accessor.getSessionId());

        log.info("Command = {}", accessor.getCommand());
        log.info("User = {}", accessor.getUser());
        log.info("Native headers = {}", accessor.toNativeHeaderMap());

        // Chỉ authenticate ở CONNECT
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            log.debug("Processing STOMP CONNECT frame...");

            // [1] Lấy Authorization header
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            log.debug("Authorization header: {}", authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("No Bearer token found in STOMP CONNECT.");
                throw new JwtException("Missing Bearer token.");
            }

            // [2] Cắt token
            String token = authHeader.substring(7);

            log.debug("Extracted token.");

            try {

                // [3] Extract username
                log.debug("Extracting username from JWT...");

                String username = jwtProvider.extractUsername(token);

                log.debug("Username extracted: {}", username);

                // [4] Load UserDetails
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                log.debug("UserDetails loaded: {}", userDetails.getUsername());
                log.debug("Authorities: {}", userDetails.getAuthorities());

                // [5] Validate token
                log.debug("Validating JWT...");

                boolean valid =
                        jwtProvider.isTokenValid(token, userDetails);

                log.debug("Token validation result: {}", valid);

                if (!valid) {
                    log.warn("JWT validation failed for user: {}", username);
                    throw new JwtException("Invalid JWT token.");
                }

                // [6] Tạo Authentication
                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // [7] Gắn Principal vào WebSocket Session
                accessor.setUser(authentication);

                log.info("STOMP authenticated user: {}", username);

            } catch (JwtException e) {

                log.warn("JWT ERROR: {}", e.getMessage());
                throw e;

            } catch (Exception e) {

                log.error("Unexpected error during STOMP authentication.", e);
                throw e;
            }
        }

        log.debug("=== STOMP INTERCEPTOR END ===");

        return message;
    }
}