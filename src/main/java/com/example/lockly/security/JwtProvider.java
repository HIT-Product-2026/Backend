package com.example.lockly.security;

import com.example.lockly.domain.entity.main.User;
import com.example.lockly.repository.main.InvalidatedTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtProvider {

    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.secret}")
    String secretKey;

    // 1. Tạo JWT Token (Đã chuyển hoàn toàn sang JJWT và BỎ ROLE)
    public String generateToken(User user, long expirationTime) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setId(UUID.randomUUID().toString()) // jti dùng cho logout/blacklist
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Tạo secret key dạng mã hóa từ chuỗi cấu hình cấu hình trong application.properties
    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Lấy username (subject) từ JWT
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Lấy thời gian hết hạn (exp) từ JWT
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Lấy JWT ID (jti) - định danh duy nhất của token để check blacklist
    public String extractTokenId(String token) {
        return extractClaim(token, Claims::getId);
    }

    // Hàm generic dùng để lấy bất kỳ claim nào từ JWT
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Kiểm tra token có hợp lệ hay không
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        final String jwtId = extractTokenId(token);

        // Kiểm tra token có nằm trong danh sách bị vô hiệu hóa (đã logout) hay không
        boolean isInvalidated = invalidatedTokenRepository.existsById(jwtId);

        // Token hợp lệ khi: Đúng username, chưa hết hạn và chưa từng logout
        return (username.equals(userDetails.getUsername()))
                && !isTokenExpired(token)
                && !isInvalidated;
    }

    // Kiểm tra token đã hết hạn chưa
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Giải mã toàn bộ claims trong JWT và kiểm tra chữ ký
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}