package com.example.simpleboard.smtp.service;

import com.example.simpleboard.account.user.exception.DuplicateAccountException;
import com.example.simpleboard.account.user.repository.LoginRepository;
import com.example.simpleboard.common.response.message.AccountMessage;
import com.example.simpleboard.common.response.message.MailMessage;
import com.example.simpleboard.smtp.config.MailVerificationProperties;
import com.example.simpleboard.smtp.entity.MailCert;
import com.example.simpleboard.smtp.exception.MailException;
import com.example.simpleboard.smtp.repository.MailCertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * 회원가입 전 이메일 소유를 확인한다.
 *
 * 인증 상태는 MailCert 행에 남고, 회원가입이 그 행을 확인한 뒤 지운다.
 * 그래서 /registers 를 직접 호출해도 인증을 건너뛸 수 없다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final MailCertRepository mailCertRepository;
    private final LoginRepository loginRepository;
    private final VerificationMailSender mailSender;
    private final MailVerificationProperties properties;

    @Transactional
    public void sendVerificationCode(String email) {
        // 이미 가입된 주소로는 보내지 않는다. 어차피 그 주소로는 가입할 수 없다.
        if (loginRepository.existsByEmail(email)) {
            throw new DuplicateAccountException(AccountMessage.EXISTS_EMAIL);
        }

        LocalDateTime now = LocalDateTime.now();
        String code = createVerificationCode();

        MailCert mailCert = mailCertRepository.findByEmail(email).orElse(null);

        if (mailCert == null) {
            mailCert = mailCertRepository.save(
                    MailCert.createMailCert(email, code, now, properties.getCodeTtl()));
        } else {
            if (mailCert.isResendTooSoon(now, properties.getResendCooldown())) {
                throw new MailException(MailMessage.RESEND_TOO_SOON);
            }

            mailCert.issue(code, now, properties.getCodeTtl());
        }

        // 발송이 실패하면 예외가 올라가고 위에서 저장한 코드도 롤백된다.
        mailSender.send(email, code);
    }

    /**
     * noRollbackFor 가 핵심이다.
     * 코드가 틀리면 MailCert 의 실패 횟수를 올리고 예외를 던지는데, 기본 설정이면 그 예외 때문에
     * 실패 횟수 증가까지 함께 롤백되어 시도 제한이 아무 의미가 없어진다.
     */
    @Transactional(noRollbackFor = MailException.class)
    public void checkVerificationCode(String email, String code) {
        findMailCert(email).verify(code, LocalDateTime.now(), properties.getMaxAttempts());
    }

    /**
     * 회원가입이 호출한다. 인증을 마친 주소인지 확인하고 그 기록을 소비한다.
     * 한 번 쓴 인증으로 두 번 가입할 수 없다.
     */
    @Transactional
    public void consumeVerifiedEmail(String email) {
        MailCert mailCert = mailCertRepository.findByEmail(email)
                .orElseThrow(() -> new MailException(MailMessage.NOT_VERIFIED_EMAIL));

        if (!mailCert.isVerifiedWithin(LocalDateTime.now(), properties.getVerifiedTtl())) {
            throw new MailException(MailMessage.NOT_VERIFIED_EMAIL);
        }

        mailCertRepository.delete(mailCert);
    }

    private MailCert findMailCert(String email) {
        return mailCertRepository.findByEmail(email)
                .orElseThrow(() -> new MailException(MailMessage.NOT_REQUESTED));
    }

    /** 사용자가 손으로 옮겨 적어야 하므로 6자리 숫자로 만든다. */
    private String createVerificationCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
