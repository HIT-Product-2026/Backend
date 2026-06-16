package com.example.lockly.security;

import com.example.lockly.domain.entity.User;
import com.example.lockly.exception.InternalServerException;
import com.example.lockly.repository.InvalidatedTokenRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.nimbusds.jwt.JWTClaimsSet;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.security.Key;
import java.util.Date;
import java.util.List;
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

    public String generateToken(User user, long expirationTime){
        try{
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + expirationTime))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("authorities", List.of("ROLE_" + user.getRole().name()))
                    .claim("userId", user.getId())
                    .claim("email", user.getEmail())
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

            signedJWT.sign(new MACSigner(secretKey.getBytes()));

            return signedJWT.serialize();
        } catch (JOSEException e){
            throw new InternalServerException("Error while signing JWT");
        }
    }

    // Tạo secret key để kiểm tra chữ ký JWT (đảm bảo token không bị sửa đổi)
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

    // Hàm generic dùng để lấy bất kỳ claim nào từ JWT
// claimsResolver giúp chọn field cần lấy (subject, exp, jti,...)
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Kiểm tra token có hợp lệ hay không
    public boolean isTokenValid(String token, UserDetails userDetails) {

        // Lấy username từ token
        final String username = extractUsername(token);

        // Lấy JWT ID (jti) để kiểm tra blacklist
        final String jwtId = extractTokenId(token);

        // Kiểm tra token có nằm trong danh sách bị vô hiệu hóa (logout)
        boolean isInvalidated = invalidatedTokenRepository.existsById(jwtId);

        // Token hợp lệ khi:
        // 1. Username đúng với user hiện tại
        // 2. Token chưa hết hạn
        // 3. Token chưa bị vô hiệu hóa (logout)
        return (username.equals(userDetails.getUsername()))
                && !isTokenExpired(token)
                && !isInvalidated;
    }

    // Kiểm tra token đã hết hạn chưa
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Lấy JWT ID (jti) - định danh duy nhất của token
    public String extractTokenId(String token) {
        return extractClaim(token, Claims::getId);
    }

    // Giải mã toàn bộ claims trong JWT và kiểm tra chữ ký
// Nếu secretKey sai hoặc token bị sửa → sẽ throw exception
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                // sử dụng secret key để verify chữ ký
                .setSigningKey(getSignInKey())
                .build()
                // parse token và lấy phần payload (claims)
                .parseClaimsJws(token)
                .getBody();
    }
}
