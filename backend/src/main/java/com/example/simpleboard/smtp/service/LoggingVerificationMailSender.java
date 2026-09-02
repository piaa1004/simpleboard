package com.example.simpleboard.smtp.service;

import lombok.extern.slf4j.Slf4j;

/**
 * SMTP 계정 없이 개발·테스트할 때 쓰는 구현. 인증번호를 콘솔에 찍는다.
 *
 * 인증번호가 로그에 그대로 남으므로 운영에서 쓰면 안 된다.
 * app.mail.enabled 를 true 로 두면 SMTP 구현이 대신 등록된다.
 */
@Slf4j
public class LoggingVerificationMailSender implements VerificationMailSender {

    @Override
    public void send(String email, String code) {
        log.warn("[개발용] 메일 발송이 꺼져 있습니다. {} 의 인증번호는 {} 입니다.", email, code);
    }
}
