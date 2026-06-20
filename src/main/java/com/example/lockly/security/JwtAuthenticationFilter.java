package com.example.lockly.security;

<<<<<<< HEAD

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.constant.ErrorMessage;
import com.example.lockly.repository.InvalidatedTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
=======
import com.example.lockly.common.ApiResponse;
import com.example.lockly.constant.ErrorMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
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
<<<<<<< HEAD
import java.text.ParseException;
=======
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe

@Slf4j
@Component
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    JwtProvider jwtProvider;
<<<<<<< HEAD

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
=======
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
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
            filterChain.doFilter(request, response);
            return;
        }

<<<<<<< HEAD
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
=======
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
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
