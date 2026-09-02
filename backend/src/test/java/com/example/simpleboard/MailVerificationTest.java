package com.example.simpleboard;

import com.example.simpleboard.smtp.entity.MailCert;
import com.example.simpleboard.smtp.repository.MailCertRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 회원가입 이메일 인증. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class MailVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MailCertRepository mailCertRepository;

    @Test
    @DisplayName("인증하지 않은 이메일로는 가입할 수 없다")
    void registerWithoutVerification() throws Exception {
        mockMvc.perform(register("nomail", "nomail@example.com"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("인증번호를 확인해야 가입할 수 있고, 같은 인증을 두 번 쓸 수는 없다")
    void verifyThenRegister() throws Exception {
        String email = "verify@example.com";

        sendCode(email);
        check(email, codeOf(email)).andExpect(status().isOk());

        mockMvc.perform(register("verified", email)).andExpect(status().isOk());

        // 인증 기록은 가입할 때 소비된다. 같은 인증으로 두 번째 계정을 만들 수 없다.
        assertThat(mailCertRepository.findByEmail(email)).isEmpty();
        mockMvc.perform(register("verified2", email)).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("틀린 인증번호는 실패 횟수로 쌓이고, 한도를 넘으면 맞는 번호도 받지 않는다")
    void wrongCodeCountsUp() throws Exception {
        String email = "attempts@example.com";
        sendCode(email);

        String correct = codeOf(email);
        String wrong = correct.equals("000000") ? "111111" : "000000";

        // 실패 횟수 증가가 예외와 함께 롤백되면 이 단정이 깨진다.
        for (int i = 1; i <= 5; i++) {
            check(email, wrong).andExpect(status().isBadRequest());
            assertThat(failedAttemptsOf(email)).isEqualTo(i);
        }

        // 한도를 넘었으므로 올바른 번호도 거절된다.
        check(email, correct).andExpect(status().isBadRequest());
        mockMvc.perform(register("attempts", email)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("재발송은 쿨다운 안에서 막고, 이미 가입된 이메일에는 보내지 않는다")
    void resendAndDuplicate() throws Exception {
        String email = "resend@example.com";

        sendCode(email).andExpect(status().isOk());
        sendCode(email).andExpect(status().isBadRequest());

        // 가입까지 마친 주소로는 인증번호 자체를 보내지 않는다.
        check(email, codeOf(email)).andExpect(status().isOk());
        mockMvc.perform(register("resender", email)).andExpect(status().isOk());
        sendCode(email).andExpect(status().isConflict());
    }

    private org.springframework.test.web.servlet.ResultActions sendCode(String email) throws Exception {
        return mockMvc.perform(post("/mail/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s"}
                        """.formatted(email)));
    }

    private org.springframework.test.web.servlet.ResultActions check(String email, String code) throws Exception {
        return mockMvc.perform(post("/mail/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s","code":"%s"}
                        """.formatted(email, code)));
    }

    private org.springframework.test.web.servlet.RequestBuilder register(String id, String email) {
        return post("/registers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id":"%s","email":"%s","password":"secret1","checkPassword":"secret1"}
                        """.formatted(id, email));
    }

    private String codeOf(String email) {
        return mailCert(email).getVerificationCode();
    }

    private int failedAttemptsOf(String email) {
        return mailCert(email).getFailedAttempts();
    }

    private MailCert mailCert(String email) {
        return mailCertRepository.findByEmail(email).orElseThrow();
    }
}
