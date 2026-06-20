package com.example.lockly.security;

import com.example.lockly.domain.entity.User;
<<<<<<< HEAD
import com.example.lockly.exception.InternalServerException;
import com.example.lockly.repository.InvalidatedTokenRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.SignedJWT;
=======
import com.example.lockly.repository.InvalidatedTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
<<<<<<< HEAD
import com.nimbusds.jwt.JWTClaimsSet;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.security.Key;
import java.util.Date;
import java.util.List;
=======

import java.security.Key;
import java.util.Date;
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
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

<<<<<<< HEAD
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
=======
    // 1. Tạo JWT Token (Đã chuyển hoàn toàn sang JJWT và BỎ ROLE)
    public String generateToken(User user, long expirationTime) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setId(UUID.randomUUID().toString()) // jti dùng cho logout/blacklist
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Tạo secret key dạng mã hóa từ chuỗi cấu hình cấu hình trong application.properties
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
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

<<<<<<< HEAD
    // Hàm generic dùng để lấy bất kỳ claim nào từ JWT
// claimsResolver giúp chọn field cần lấy (subject, exp, jti,...)
=======
    // Lấy JWT ID (jti) - định danh duy nhất của token để check blacklist
    public String extractTokenId(String token) {
        return extractClaim(token, Claims::getId);
    }

    // Hàm generic dùng để lấy bất kỳ claim nào từ JWT
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Kiểm tra token có hợp lệ hay không
    public boolean isTokenValid(String token, UserDetails userDetails) {
<<<<<<< HEAD

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
=======
        final String username = extractUsername(token);
        final String jwtId = extractTokenId(token);

        // Kiểm tra token có nằm trong danh sách bị vô hiệu hóa (đã logout) hay không
        boolean isInvalidated = invalidatedTokenRepository.existsById(jwtId);

        // Token hợp lệ khi: Đúng username, chưa hết hạn và chưa từng logout
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
        return (username.equals(userDetails.getUsername()))
                && !isTokenExpired(token)
                && !isInvalidated;
    }

    // Kiểm tra token đã hết hạn chưa
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

<<<<<<< HEAD
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
=======
    // Giải mã toàn bộ claims trong JWT và kiểm tra chữ ký
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
                .parseClaimsJws(token)
                .getBody();
    }
}
