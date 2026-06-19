package com.example.lockly.security;

import com.example.lockly.common.ApiResponse;
import com.example.lockly.constant.ErrorMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    JwtProvider jwtProvider;
    UserDetailsService userDetailsService;

    // FIX: bỏ InvalidatedTokenRepository khỏi đây
    // — việc check blacklist đã có sẵn bên trong JwtProvider.isTokenValid()
    // — filter không cần import Nimbus nữa

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // [1] Lấy header Authorization
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // [2] Cắt lấy JWT (bỏ "Bearer ")
        String token = authHeader.substring(7);

        try {
            // [3] Extract username — JJWT tự verify signature + expiry ở đây
            //     Nếu token lỗi format / sai signature → JwtException được ném ra
            String username = jwtProvider.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // [4] isTokenValid kiểm tra thêm: username khớp + chưa hết hạn + chưa blacklist
                if (jwtProvider.isTokenValid(token, userDetails)) {
                    log.debug("Token valid for user: {}", username);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.info("Authenticated user: {}, Authorities: {}",
                            username, userDetails.getAuthorities());
                } else {
                    log.warn("Token failed validation for user: {}", username);
                }
            }

        } catch (JwtException e) {
            // Token sai format / signature / đã hết hạn
            log.warn("JWT error: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    ErrorMessage.Auth.ERR_TOKEN_INVALIDATED);
            return;
        } catch (Exception e) {
            log.error("Unexpected error in JWT filter: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi xác thực không xác định.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(status);
        new ObjectMapper().writeValue(response.getOutputStream(),
                ApiResponse.error(status, message));
    }
}