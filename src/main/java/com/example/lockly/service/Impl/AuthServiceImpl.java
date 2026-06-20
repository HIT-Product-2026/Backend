package com.example.lockly.service.Impl;

<<<<<<< HEAD
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
=======
import com.example.lockly.common.util.PasswordUtil;
import com.example.lockly.constant.CommonConstant;
import com.example.lockly.constant.ErrorMessage;
import com.example.lockly.domain.dto.request.*;
import com.example.lockly.domain.dto.response.LoginResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;
import com.example.lockly.domain.entity.InvalidatedToken;
import com.example.lockly.domain.entity.OtpPurpose;
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
import com.example.lockly.domain.entity.User;
import com.example.lockly.exception.BadRequestException;
import com.example.lockly.exception.DuplicateResourceException;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.exception.VsException;
import com.example.lockly.repository.InvalidatedTokenRepository;
import com.example.lockly.repository.UserRepository;
import com.example.lockly.security.JwtProvider;
import com.example.lockly.service.AuthService;
<<<<<<< HEAD
import com.example.lockly.service.UserService;
import com.nimbusds.jwt.SignedJWT;
=======
import com.example.lockly.service.OtpService;
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
<<<<<<< HEAD
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
=======
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    JwtProvider jwtProvider;
<<<<<<< HEAD
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
=======
    InvalidatedTokenRepository invalidatedTokenRepository;
    OtpService otpService;
    PasswordUtil passwordUtil;

    @NonFinal @Value("${jwt.access.expiration_time}")  long ACCESS_TOKEN_EXPIRATION;
    @NonFinal @Value("${jwt.refresh.expiration_time}") long REFRESH_TOKEN_EXPIRATION;

    @Override
    @Transactional
    public void sendOtpForRegister(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }
        otpService.sendOtp(request.email(), OtpPurpose.REGISTER);
    }

    @Override
    @Transactional
    public UserResponseDto verifyOtpAndRegister(VerifyOtpRegisterRequestDto request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException(ErrorMessage.Auth.ERR_PASSWORD_NOT_MATCH);
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("User", "username", request.username());
        }
        otpService.verifyOtp(request.email(), request.otp(), OtpPurpose.REGISTER);

        User user = User.builder()
                .username(request.username())
                .displayName(request.displayName())
                .email(request.email())
                .password(passwordUtil.hash(request.password()))
                .build();

>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
        userRepository.save(user);
        return UserResponseDto.from(user);
    }

    @Override
    @Transactional(readOnly = true)
<<<<<<< HEAD
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

=======
    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException(ErrorMessage.Auth.ERR_INVALID_CREDENTIALS));

        if (!passwordUtil.verify(request.password(), user.getPassword())) {
            throw new BadRequestException(ErrorMessage.Auth.ERR_INVALID_CREDENTIALS);
        }
        return buildLoginResponse(user);
    }

    @Override
    @Transactional
    public void logout(LogoutRequestDto request) {
        String token = request.token();

        if (jwtProvider.isTokenExpired(token)) {
            throw new VsException(HttpStatus.UNAUTHORIZED, ErrorMessage.Auth.ERR_TOKEN_INVALIDATED);
        }
        String jwtId = jwtProvider.extractTokenId(token);
        if (invalidatedTokenRepository.existsById(jwtId)) {
            throw new VsException(HttpStatus.BAD_REQUEST, ErrorMessage.Auth.ERR_TOKEN_ALREADY_INVALIDATED);
        }

        Date expirationDate = jwtProvider.extractExpiration(token);
        LocalDateTime expirationTime = expirationDate.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        invalidatedTokenRepository.save(new InvalidatedToken(jwtId, expirationTime));
    }

    @Override
    @Transactional
    public void sendOtpForForgotPassword(ForgotPasswordRequestDto request) {
        if (!userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Nếu email tồn tại, mã OTP sẽ được gửi tới Gmail của bạn.");
        }
        otpService.sendOtp(request.email(), OtpPurpose.FORGOT_PASSWORD);
    }

    @Override
    @Transactional
    public void verifyOtpForgotPassword(VerifyOtpForgotPasswordRequestDto request) {
        otpService.verifyOtp(request.email(), request.otp(), OtpPurpose.FORGOT_PASSWORD);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException(ErrorMessage.Auth.ERR_PASSWORD_NOT_MATCH);
        }
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.email()));

        user.setPassword(passwordUtil.hash(request.newPassword()));
        userRepository.save(user);
    }

    private LoginResponseDto buildLoginResponse(User user) {
        String accessToken  = jwtProvider.generateToken(user, ACCESS_TOKEN_EXPIRATION);
        String refreshToken = jwtProvider.generateToken(user, REFRESH_TOKEN_EXPIRATION);
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .id(user.getId())
                .tokenType(CommonConstant.BEARER_TOKEN)
                .build();
    }
<<<<<<< HEAD

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponseDto logout(LogoutRequestDto request) {
        try{
            // Tách các thành phần trong token
            SignedJWT signedJWT = SignedJWT.parse(request.token());

            // [2]. Get username and get user details to validate token
            String username = jwtProvider.extractUsername(request.token());
            UserDetails userDetails = userService.loadUserByUsername(username);

            // [3]. Validate token
            if (!jwtProvider.isTokenValid(request.token(), userDetails)) {
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
=======
}
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
