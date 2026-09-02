package com.example.simpleboard.security.exception;

/** 서명이 맞지 않거나 서버가 발급한 적 없는 토큰. */
public class TokenForgeryException extends RuntimeException {

    public TokenForgeryException(String message) {
        super(message);
    }
}
