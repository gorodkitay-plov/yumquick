package com.yumquick.common.exception;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {

    private final HttpStatus status;

    public AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    // ── 404 ──────────────────────────────────────────────
    public static AppException notFound(String message) {
        return new AppException(HttpStatus.NOT_FOUND, message);
    }

    // ── 400 ──────────────────────────────────────────────
    public static AppException badRequest(String message) {
        return new AppException(HttpStatus.BAD_REQUEST, message);
    }

    // ── 401 ──────────────────────────────────────────────
    public static AppException unauthorized(String message) {
        return new AppException(HttpStatus.UNAUTHORIZED, message);
    }

    // ── 403 ──────────────────────────────────────────────
    public static AppException forbidden(String message) {
        return new AppException(HttpStatus.FORBIDDEN, message);
    }

    // ── 409 ──────────────────────────────────────────────
    public static AppException conflict(String message) {
        return new AppException(HttpStatus.CONFLICT, message);
    }
}
