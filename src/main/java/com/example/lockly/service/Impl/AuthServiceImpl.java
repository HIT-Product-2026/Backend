package com.example.lockly.service.Impl;

import com.example.lockly.constant.CommonConstant;
import com.example.lockly.common.util.PasswordUtil;
import com.example.lockly.constant.ErrorMessage;
import com.example.lockly.constant.SuccessMessage;
import com.example.lockly.domain.dto.request.LoginRequestDto;
import com.example.lockly.domain.dto.request.LogoutRequestDto;
import com.example.lockly.domain.dto.request.RegisterRequestDto;
import com.example.lockly.domain.dto.response.CommonResponseDto;
import com.example.lockly.domain.dto.response.LoginResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;
import com.example.lockly.domain.entity.InvalidatedToken;
import com.example.lockly.domain.entity.User;
import com.example.lockly.exception.BadRequestException;
import com.example.lockly.exception.DuplicateResourceException;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.exception.VsException;
import com.example.lockly.repository.InvalidatedTokenRepository;
import com.example.lockly.repository.UserRepository;
import com.example.lockly.security.JwtProvider;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.UserService;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    JwtProvider jwtProvider;
    UserService userService;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.access.expiration_time}")
    long ACCESS_TOKEN_EXPIRATION;

    @NonFinal
    @Value("${jwt.refresh.expiration_time}")
    long REFRESH_TOKEN_EXPIRATION;

    @Override
    @Transactional
    public UserResponseDto register(RegisterRequestDto request){
        if (userRepository.existsByEmail(request.email()))
            throw new DuplicateResourceException("User", "email", request.email());

        if (userRepository.existsByUsername(request.username()))
            throw new DuplicateResourceException("User", "username", request.username());

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(PasswordUtil.hash(request.password()))
                .build();
        userRepository.save(user);
        return UserResponseDto.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto authentication(LoginRequestDto request) {

        // Check username
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User",
                                "username",
                                request.username()));

        // Check password
        boolean valid = PasswordUtil.verify(request.password(), user.getPassword());
        if (!valid) {
            throw new BadRequestException("Invalid credentials");
        }

        // Generate token
        String accessToken = jwtProvider.generateToken(user, ACCESS_TOKEN_EXPIRATION);
        String refreshToken = jwtProvider.generateToken(user, REFRESH_TOKEN_EXPIRATION);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .id(user.getId())
                .tokenType(CommonConstant.BEARER_TOKEN)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponseDto logout(LogoutRequestDto request) {
        try{
            // Tách các thành phần trong token
            SignedJWT signedJWT = SignedJWT.parse(request.getToken());

            // [2]. Get username and get user details to validate token
            String username = jwtProvider.extractUsername(request.getToken());
            UserDetails userDetails = userService.loadUserByUsername(username);

            // [3]. Validate token
            if (!jwtProvider.isTokenValid(request.getToken(), userDetails)) {
                throw new VsException(HttpStatus.UNAUTHORIZED, ErrorMessage.Auth.ERR_TOKEN_INVALIDATED);
            }

            // [4]. Get JWT ID and expiration time to invalidate token
            String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
            Date expirationDate = signedJWT.getJWTClaimsSet().getExpirationTime();
            LocalDateTime expirationTime = expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            if (invalidatedTokenRepository.existsById(jwtId)) {
                throw new VsException(HttpStatus.BAD_REQUEST, ErrorMessage.Auth.ERR_TOKEN_ALREADY_INVALIDATED);
            }

            // [5]. Invalidate access token
            // This step will save the JWT ID and expiration time of the token to the
            // database, so that when the user tries to use the token again, we can check if
            // it has been invalidated or not.
            invalidatedTokenRepository.save(new InvalidatedToken(jwtId, expirationTime));

            return new CommonResponseDto(HttpStatus.OK, SuccessMessage.Auth.LOGOUT_SUCCESS);
        } catch (ParseException e) {
            throw new VsException(HttpStatus.BAD_REQUEST, ErrorMessage.Auth.ERR_GET_TOKEN_CLAIM_SET_FAIL);
        }
    }
}
