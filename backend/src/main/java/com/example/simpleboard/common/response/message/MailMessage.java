package com.example.simpleboard.common.response.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MailMessage {
    SMTP_SERVER_ERROR("인증 메일을 보내지 못했습니다. 잠시 후 다시 시도해주세요."),
    NOT_REQUESTED("인증번호를 먼저 요청해주세요."),
    NOT_MATCHED_CODE("인증번호가 일치하지 않습니다."),
    EXPIRED_CODE("인증번호가 만료되었습니다. 다시 요청해주세요."),
    TOO_MANY_ATTEMPTS("인증 시도 횟수를 초과했습니다. 인증번호를 다시 요청해주세요."),
    RESEND_TOO_SOON("인증번호를 방금 보냈습니다. 잠시 후 다시 시도해주세요."),
    NOT_VERIFIED_EMAIL("이메일 인증을 먼저 완료해주세요.");

    private final String message;
}
