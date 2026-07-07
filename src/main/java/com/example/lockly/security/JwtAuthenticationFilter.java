package com.example.lockly.security;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.constant.ErrorMessage;
import com.example.lockly.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
//@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    JwtProvider jwtProvider;
    UserService userService;
    ObjectMapper objectMapper;

    // FIX: bỏ InvalidatedTokenRepository khỏi đây
    // — việc check blacklist đã có sẵn bên trong JwtProvider.isTokenValid()
    // — filter không cần import Nimbus nữa

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // [0] LOG REQUEST INCOMING
        log.debug("=== JWT FILTER START ===");
        log.debug("Request URI: {}", request.getRequestURI());
        log.debug("Method: {}", request.getMethod());

        // [1] Lấy header Authorization
        String authHeader = request.getHeader("Authorization");
        log.debug("Authorization header: {}", authHeader);
        log.debug("HEADERS = {}", Collections.list(request.getHeaderNames()));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // [2] Cắt lấy JWT (bỏ "Bearer ")
        String token = authHeader.substring(7);
        log.debug("Extracted token (raw): {}", token);

        try {
            // [3] Extract username — JJWT tự verify signature + expiry ở đây
            //     Nếu token lỗi format / sai signature → JwtException được ném ra
            log.debug("Attempting to extract username from token...");
            String username = jwtProvider.extractUsername(token);
            log.debug("Extracted username: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                log.debug("No existing authentication in SecurityContext, loading user: {}", username);

                UserDetails userDetails = userService.loadUserByUsername(username);
                log.debug("UserDetails loaded: {}", userDetails.getUsername());
                log.debug("Authorities from DB: {}", userDetails.getAuthorities());

                // [4] isTokenValid kiểm tra thêm: username khớp + chưa hết hạn + chưa blacklist
                log.debug("Validating token...");
                boolean valid = jwtProvider.isTokenValid(token, userDetails);
                log.debug("Token validation result: {}", valid);

                if (valid) {
                    log.info("Token valid for user: {}", username);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.info("Authenticated user: {}, Authorities: {}",
                            username, userDetails.getAuthorities());

                } else {
                    log.warn("Token failed validation for user: {}", username);
                }
            } else {
                log.debug("Skip authentication: username={}, existingAuth={}",
                        username,
                        SecurityContextHolder.getContext().getAuthentication() != null
                );
            }

        } catch (JwtException e) {
            // Token sai format / signature / đã hết hạn
            log.warn("JWT ERROR (JwtException): {}", e.getMessage());
            log.warn("Request URI: {}", request.getRequestURI());

            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    ErrorMessage.Auth.ERR_TOKEN_INVALIDATED);
            return;

        } catch (Exception e) {
            log.error("Unexpected error in JWT filter", e);
            log.error("Request URI: {}", request.getRequestURI());

            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi xác thực không xác định.");
            return;
        }

        log.debug("Passing request to next filter: {}", request.getRequestURI());
        filterChain.doFilter(request, response);

        log.debug("=== JWT FILTER END ===");
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(status);
        objectMapper.writeValue(response.getOutputStream(),
                ApiResponse.error(status, message));
    }
}