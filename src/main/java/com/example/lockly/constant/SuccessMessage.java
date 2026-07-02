package com.example.lockly.constant;

public class SuccessMessage {
    public static class Auth {

        public static final String LOGIN_SUCCESS = "Đăng nhập thành công!";
        public static final String LOGOUT_SUCCESS = "Đăng xuất thành công.";
        public static final String REGISTER_SUCCESS = "Đăng ký tài khoản thành công!";
        public static final String SEND_OTP_SUCCESS = "Mã OTP đã được gửi về Gmail của bạn. Vui lòng kiểm tra trong vòng 5 phút.";
        public static final String VERIFY_OTP_SUCCESS = "Xác thực OTP thành công.";
        public static final String RESET_PASSWORD_SUCCESS = "Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại.";
    }

    public static class User {
        public static final String GET_MY_INFO_SUCCESS = "Lấy thông tin người dùng thành công.";
    }
}
