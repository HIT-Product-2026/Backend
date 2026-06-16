package com.example.lockly.service.Impl;

import com.example.lockly.constant.CommonConstant;
import com.example.lockly.common.util.PasswordUtil;
import com.example.lockly.constant.ErrorMessage;
import com.example.lockly.constant.SuccessMessage;
import com.example.lockly.domain.dto.request.*;
import com.example.lockly.domain.dto.response.CommonResponseDto;
import com.example.lockly.domain.dto.response.LoginResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;
import com.example.lockly.domain.entity.InvalidatedToken;
import com.example.lockly.domain.entity.OtpPurpose;
import com.example.lockly.domain.entity.User;
import com.example.lockly.domain.entity.UserRole;
import com.example.lockly.exception.BadRequestException;
import com.example.lockly.exception.DuplicateResourceException;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.exception.VsException;
import com.example.lockly.repository.InvalidatedTokenRepository;
import com.example.lockly.repository.UserRepository;
import com.example.lockly.security.JwtProvider;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.OtpService;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    JwtProvider jwtProvider;
    UserService userService;
    InvalidatedTokenRepository invalidatedTokenRepository;
    OtpService otpService;   // <-- inject OtpService mới

    @NonFinal
    @Value("${jwt.access.expiration_time}")
    long ACCESS_TOKEN_EXPIRATION;

    @NonFinal
    @Value("${jwt.refresh.expiration_time}")
    long REFRESH_TOKEN_EXPIRATION;

    @Override
    @Transactional
    public UserResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.email()))
            throw new DuplicateResourceException("User", "email", request.email());

        if (userRepository.existsByUsername(request.username()))
            throw new DuplicateResourceException("User", "username", request.username());

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(PasswordUtil.hash(request.password()))
                .role(UserRole.USER)
                .build();
        userRepository.save(user);
        return UserResponseDto.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto authentication(LoginRequestDto request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", request.username()));

        boolean valid = PasswordUtil.verify(request.password(), user.getPassword());
        if (!valid) {
            throw new BadRequestException("Sai tên đăng nhập hoặc mật khẩu.");
        }

        return buildLoginResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponseDto logout(LogoutRequestDto request) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(request.getToken());

            String username = jwtProvider.extractUsername(request.getToken());
            UserDetails userDetails = userService.loadUserByUsername(username);

            if (!jwtProvider.isTokenValid(request.getToken(), userDetails)) {
                throw new VsException(HttpStatus.UNAUTHORIZED, ErrorMessage.Auth.ERR_TOKEN_INVALIDATED);
            }

            String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
            Date expirationDate = signedJWT.getJWTClaimsSet().getExpirationTime();
            LocalDateTime expirationTime = expirationDate.toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();

            if (invalidatedTokenRepository.existsById(jwtId)) {
                throw new VsException(HttpStatus.BAD_REQUEST, ErrorMessage.Auth.ERR_TOKEN_ALREADY_INVALIDATED);
            }

            invalidatedTokenRepository.save(new InvalidatedToken(jwtId, expirationTime));

            return new CommonResponseDto(HttpStatus.OK, SuccessMessage.Auth.LOGOUT_SUCCESS);
        } catch (ParseException e) {
            throw new VsException(HttpStatus.BAD_REQUEST, ErrorMessage.Auth.ERR_GET_TOKEN_CLAIM_SET_FAIL);
        }
    }


    @Override
    @Transactional
    public CommonResponseDto sendOtpForRegister(SendOtpRequestDto request) {
        // Kiểm tra số điện thoại đã được dùng chưa
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new DuplicateResourceException("User", "phoneNumber", request.phoneNumber());
        }

        otpService.sendOtp(request.phoneNumber(), OtpPurpose.REGISTER);

        return new CommonResponseDto(
                HttpStatus.OK,
                "Mã OTP đã được gửi tới Zalo của bạn. Vui lòng kiểm tra trong " +
                        "vòng 5 phút."
        );
    }


    @Override
    @Transactional
    public UserResponseDto verifyOtpAndRegister(VerifyOtpRegisterRequestDto request) {
        // Xác thực OTP (throw exception nếu sai/hết hạn)
        otpService.verifyOtp(request.phoneNumber(), request.otp(), OtpPurpose.REGISTER);

        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("User", "username", request.username());
        }

        // Kiểm tra số điện thoại đã được đăng ký chưa (race condition check)
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new DuplicateResourceException("User", "phoneNumber", request.phoneNumber());
        }

        // Tạo user mới
        // Không có password vì đăng nhập bằng OTP
        // Dùng UUID ngẫu nhiên làm placeholder password (không bao giờ dùng thực tế)
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(request.username())
                .displayName(request.displayName())
                .phoneNumber(request.phoneNumber())
                .password(PasswordUtil.hash(UUID.randomUUID().toString())) // placeholder
                .role(UserRole.USER)
                .build();

        userRepository.save(user);

        return UserResponseDto.from(user);
    }


    @Override
    @Transactional
    public CommonResponseDto sendOtpForLogin(SendOtpRequestDto request) {
        // Kiểm tra số điện thoại có tồn tại trong hệ thống không
        if (!userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new ResourceNotFoundException("User", "phoneNumber", request.phoneNumber());
        }

        otpService.sendOtp(request.phoneNumber(), OtpPurpose.LOGIN);

        return new CommonResponseDto(
                HttpStatus.OK,
                "Mã OTP đã được gửi tới Zalo của bạn. Vui lòng kiểm tra trong " +
                        "vòng 5 phút."
        );
    }

    @Override
    @Transactional
    public LoginResponseDto verifyOtpAndLogin(VerifyOtpLoginRequestDto request) {
        // Xác thực OTP
        otpService.verifyOtp(request.phoneNumber(), request.otp(), OtpPurpose.LOGIN);

        // Tìm user theo số điện thoại
        User user =userRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("User", "phoneNumber", request.phoneNumber()));

        // Cấp JWT token
        return buildLoginResponse(user);
    }


    private LoginResponseDto buildLoginResponse(User user) {
        String accessToken = jwtProvider.generateToken(user, ACCESS_TOKEN_EXPIRATION);
        String refreshToken = jwtProvider.generateToken(user, REFRESH_TOKEN_EXPIRATION);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .id(user.getId())
                .tokenType(CommonConstant.BEARER_TOKEN)
                .build();
    }
}