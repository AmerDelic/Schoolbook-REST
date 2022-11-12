package com.amerd.schoolbook.common.constant;

public class SecurityConstant {
    public static final long EXPIRATION_TIME = 432_000_000L;// 5 days in ms
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String JWT_TOKEN_HEADER = "Jwt-Token";
    public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";
    public static final String ISSUER_INFO = "SchoolBook app";
    public static final String USER_ADMINISTRATION = "User Admin Portal";
    public static final String AUTHORITIES = "Authorities";
    public static final String FORBIDDEN_MESSAGE = "You need to log in to access this page";
    public static final String ACCESS_DENIED_MESSAGE = "You do not have permission to access this page";
    public static final String[] PUBLIC_URLS = {"/error", "/user/new" ,"/user/login", "/user/register", "/user/reset-pass/**"};
}
