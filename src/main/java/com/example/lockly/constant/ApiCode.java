package com.example.lockly.constant;

public class ApiCode {

    // ================= SUCCESS =================
    public static final int SUCCESS = 0;
    public static final int CREATED = 1;

    // ================= CLIENT ERROR (1xxx) =================
    public static final int BAD_REQUEST = 1000;
    public static final int VALIDATION_ERROR = 1001;
    public static final int NOT_FOUND = 1002;
    public static final int DUPLICATE = 1003;
    public static final int UNAUTHORIZED = 1004;
    public static final int FORBIDDEN = 1005;

    // ================= AUTH ERROR (2xxx) =================
    public static final int TOKEN_INVALID = 2000;
    public static final int TOKEN_EXPIRED = 2001;
    public static final int LOGIN_FAILED = 2002;

    // ================= SERVER ERROR (5xxx) =================
    public static final int INTERNAL_ERROR = 5000;
    public static final int SERVICE_UNAVAILABLE = 5001;

    private ApiCode() {
        // prevent instantiation
    }
}