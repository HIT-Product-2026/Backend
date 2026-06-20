package com.example.lockly.constant;

public class ErrorMessage {
    public static final String ERR_EXCEPTION_GENERAL = "exception.general";
    public static final String UNAUTHORIZED = "exception.unauthorized";
    public static final String FORBIDDEN = "exception.forbidden";
    public static final String BAD_REQUEST = "exception.bad.request";
    public static final String FORBIDDEN_UPDATE_DELETE = "exception.forbidden.update-delete";
    public static final String ERR_UPLOAD_IMAGE_FAIL = "exception.upload.image.fail";

    // error validation dto
    public static final String INVALID_SOME_THING_FIELD = "invalid.general";
    public static final String INVALID_FORMAT_SOME_THING_FIELD = "invalid.general.format";
    public static final String INVALID_SOME_THING_FIELD_IS_REQUIRED = "invalid.general.required";
    public static final String NOT_BLANK_FIELD = "invalid.general.not-blank";
    public static final String INVALID_FORMAT_PASSWORD = "invalid.password-format";

    public static class Auth {
        public static final String ERR_INVALID_CREDENTIALS = "exception.auth.username.or.password.wrong";
        public static final String INVALID_REFRESH_TOKEN = "exception.auth.invalid.refresh.token";
        public static final String EXPIRED_REFRESH_TOKEN = "exception.auth.expired.refresh.token";
        public static final String ERR_LOGIN_FAIL = "exception.auth.login.fail";
        public static final String ERR_GET_TOKEN_CLAIM_SET_FAIL = "exception.auth.get.token.claim.set.fail";
        public static final String ERR_TOKEN_INVALIDATED = "exception.auth.token.invalidated";
        public static final String ERR_MALFORMED_TOKEN = "exception.auth.malformed.token";
        public static final String ERR_TOKEN_ALREADY_INVALIDATED = "exception.auth.token.already.invalidated";

<<<<<<< HEAD
=======
        // Bổ sung các hằng số lỗi cho OTP & Reset Password
        public static final String ERR_INVALID_OTP = "exception.auth.invalid.otp";
        public static final String ERR_EXPIRED_OTP = "exception.auth.expired.otp";
        public static final String ERR_PASSWORD_NOT_MATCH = "exception.auth.password.not.match";
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
    }

    public static class User {
        public static final String ERR_USER_NOT_EXISTED = "exception.user.user.not.existed";
        public static final String ERR_USERNAME_EXISTED = "exception.user.username.existed";
        public static final String ERR_EMAIL_EXISTED = "exception.user.email.existed";
    }

    public static class Admin {
        public static final String ERR_NOT_ADMIN = "exception.admin.not.admin";
    }
<<<<<<< HEAD

=======
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
}