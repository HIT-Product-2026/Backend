package com.example.lockly.security;


import com.example.lockly.common.ApiResponse;
import com.example.lockly.constant.ErrorMessage;
import com.example.lockly.repository.InvalidatedTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
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
import java.text.ParseException;

@Slf4j
@Component
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    JwtProvider jwtProvider;

    UserDetailsService userDetailsService;

    InvalidatedTokenRepository invalidatedTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {


        // [1] Lấy header Authorization từ request
        // Ví dụ: Authorization: Bearer eyJhbGciOi...
        String authHeader = request.getHeader("Authorization");

        // Nếu không có token hoặc không đúng format "Bearer xxx"
        // => bỏ qua filter này, cho request đi tiếp
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found in request: {}", request.getRequestURI());

            // Không auth nhưng vẫn cho đi tiếp (endpoint public hoặc sẽ bị chặn ở nơi khác)
            filterChain.doFilter(request, response);
            return;
        }

        // [2] Cắt lấy JWT token (bỏ "Bearer ")
        String token = authHeader.substring(7);

        try {
            // [2.1] Parse JWT token (không verify signature ở đây)
            // Mục tiêu: lấy JWT ID (jti)
            SignedJWT signedJWT = SignedJWT.parse(token);

            // lấy JWT ID (unique id của token)
            String jwtId = signedJWT.getJWTClaimsSet().getJWTID();

            // [2.2] Check token đã bị revoke / logout chưa
            // Nếu JWT ID có trong DB => token không hợp lệ
            if (invalidatedTokenRepository.existsById(jwtId)) {
                sendErrorResponse(
                        response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        ErrorMessage.Auth.ERR_TOKEN_INVALIDATED
                );
                return; // dừng filter chain luôn
            }

        } catch (ParseException e) {
            // Token bị lỗi format (không parse được JWT)
            // => trả 400 Bad Request
            sendErrorResponse(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    ErrorMessage.Auth.ERR_MALFORMED_TOKEN
            );
            return;
        }

        // [3] Extract username từ JWT (qua JwtProvider của bạn)
        String username = jwtProvider.extractUsername(token);

        // chỉ authenticate nếu:
        // - có username
        // - chưa có authentication trong SecurityContext
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user từ DB (Spring Security)
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // [3.1] Validate token (signature + expiry + claims...)
            if (jwtProvider.isTokenValid(token, userDetails)) {

                log.debug("Token valid for user: {}", username);

                // [3.2] Tạo Authentication object cho Spring Security
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // attach request details (IP, session, etc.)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // [3.3] Set vào SecurityContext
                // => từ đây trở đi request được coi là "đã login"
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.info("Authenticated user: {}, Authorities: {}",
                        username,
                        userDetails.getAuthorities()
                );

            } else {
                // Token hợp lệ format nhưng sai logic (expired / wrong user / signature fail)
                log.warn("Invalid token for user: {}", username);
            }
        }

        // [4] Cho request đi tiếp sang filter tiếp theo / controller
        filterChain.doFilter(request, response);
    }

    // Helper: trả response lỗi dạng JSON khi token fail
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(status);

        // Build response body theo format ApiResponse
        ApiResponse<Object> body =
                ApiResponse.error(status, message);

        ObjectMapper mapper = new ObjectMapper();

        // ghi JSON trực tiếp ra response output stream
        mapper.writeValue(response.getOutputStream(), body);
    }
}
