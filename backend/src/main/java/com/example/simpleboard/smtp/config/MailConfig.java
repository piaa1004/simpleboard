package com.example.simpleboard.smtp.config;

import com.example.simpleboard.smtp.service.LoggingVerificationMailSender;
import com.example.simpleboard.smtp.service.SmtpVerificationMailSender;
import com.example.simpleboard.smtp.service.VerificationMailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Slf4j
@Configuration
@EnableConfigurationProperties(MailVerificationProperties.class)
public class MailConfig {

    /**
     * app.mail.enabled 가 켜져 있으면 SMTP 로 보내고, 아니면 로그로만 남긴다.
     *
     * 켜 놓고 spring.mail.host 를 빠뜨리면 JavaMailSender 빈이 아예 만들어지지 않는다.
     * 그 상태로 기동해서 첫 회원가입 때 실패하는 대신, 여기서 바로 멈추게 한다.
     */
    @Bean
    public VerificationMailSender verificationMailSender(ObjectProvider<JavaMailSender> javaMailSender,
                                                         MailVerificationProperties properties) {
        if (!properties.isEnabled()) {
            log.warn("app.mail.enabled=false 입니다. 인증번호를 메일로 보내지 않고 로그에 남깁니다.");

            return new LoggingVerificationMailSender();
        }

        JavaMailSender sender = javaMailSender.getIfAvailable();
        if (sender == null) {
            throw new IllegalStateException(
                    "app.mail.enabled=true 이지만 spring.mail.host 가 설정되지 않아 메일을 보낼 수 없습니다.");
        }

        return new SmtpVerificationMailSender(sender, properties.getFrom());
    }
}
