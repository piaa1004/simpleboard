package com.example.simpleboard.smtp.service;

/**
 * 인증 메일을 실제로 보내는 쪽. 실패하면 예외를 던진다.
 *
 * SMTP 계정 없이도 개발할 수 있도록 구현을 갈아끼울 수 있게 인터페이스로 둔다.
 */
public interface VerificationMailSender {

    void send(String email, String code);
}
