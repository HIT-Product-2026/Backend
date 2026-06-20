package com.example.lockly.service;

<<<<<<< HEAD
import com.example.lockly.domain.dto.request.LoginRequestDto;
import com.example.lockly.domain.dto.request.LogoutRequestDto;
import com.example.lockly.domain.dto.request.RegisterRequestDto;
import com.example.lockly.domain.dto.response.CommonResponseDto;
=======
import com.example.lockly.domain.dto.request.*;
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
import com.example.lockly.domain.dto.response.LoginResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;

public interface AuthService {
<<<<<<< HEAD

    public UserResponseDto register(RegisterRequestDto request);
    public LoginResponseDto authentication(LoginRequestDto request);
    public CommonResponseDto logout(LogoutRequestDto request);
}
=======
    void sendOtpForRegister(RegisterRequestDto request);
    UserResponseDto verifyOtpAndRegister(VerifyOtpRegisterRequestDto request);
    LoginResponseDto login(LoginRequestDto request);
    void logout(LogoutRequestDto request);
    void sendOtpForForgotPassword(ForgotPasswordRequestDto request);
    void verifyOtpForgotPassword(VerifyOtpForgotPasswordRequestDto request);
    void resetPassword(ResetPasswordRequestDto request);

}
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
