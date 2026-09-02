package com.example.simpleboard.smtp.service;

import com.example.simpleboard.common.response.message.MailMessage;
import com.example.simpleboard.smtp.exception.MailException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class SmtpVerificationMailSender implements VerificationMailSender {

    private final JavaMailSender javaMailSender;
    private final String from;

    @Override
    public void send(String email, String code) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setSubject("[SimpleBoard] 이메일 인증번호");
            helper.setTo(email);
            helper.setFrom(from);
            helper.setText(body(code), true);

            javaMailSender.send(message);
        } catch (Exception e) {
            // 원인은 서버 로그에만 남기고, 사용자에게는 일반적인 안내만 보낸다.
            log.error("인증 메일 발송 실패: {}", email, e);
            throw new MailException(MailMessage.SMTP_SERVER_ERROR, e);
        }
    }

    private String body(String code) {
        return """
                <div style="max-width:480px;margin:0 auto;padding:32px 24px;border:1px solid #e5e5e5;border-radius:12px;
                            font-family:'Malgun Gothic','맑은 고딕',sans-serif;color:#222;">
                  <h1 style="margin:0 0 8px;font-size:20px;">이메일 인증번호</h1>
                  <p style="margin:0 0 24px;font-size:14px;color:#666;">
                    아래 번호를 회원가입 화면의 인증번호 칸에 입력해주세요.
                  </p>
                  <p style="margin:0 0 24px;font-size:32px;font-weight:bold;letter-spacing:8px;">%s</p>
                  <p style="margin:0;font-size:12px;color:#888;">
                    본인이 요청하지 않았다면 이 메일은 무시하셔도 됩니다.
                  </p>
                </div>
                """.formatted(code);
    }
}
