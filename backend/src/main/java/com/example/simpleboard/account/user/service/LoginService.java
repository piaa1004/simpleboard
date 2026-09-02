package com.example.simpleboard.account.user.service;

import com.example.simpleboard.account.user.domain.User;
import com.example.simpleboard.account.user.dto.LoginRequest;
import com.example.simpleboard.account.user.dto.RegisterRequest;
import com.example.simpleboard.account.user.dto.UserResponse;
import com.example.simpleboard.account.user.exception.DuplicateAccountException;
import com.example.simpleboard.account.user.exception.LoginException;
import com.example.simpleboard.account.user.repository.LoginRepository;
import com.example.simpleboard.common.response.message.AccountMessage;
import com.example.simpleboard.security.jwt.dto.Token;
import com.example.simpleboard.security.jwt.service.JwtService;
import com.example.simpleboard.security.jwt.support.CookieSupport;
import com.example.simpleboard.security.jwt.support.JwtTokenProvider;
import com.example.simpleboard.smtp.service.MailService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginService {

    private final LoginRepository loginRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtService jwtService;
    private final CookieSupport cookieSupport;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Transactional
    public void register(RegisterRequest request) {
        if (!request.isPasswordConfirmed()) {
            throw new LoginException(AccountMessage.NOT_MATCH_PASSWORD);
        }

        checkAvailable(request.getId(), request.getEmail());

        // 인증을 마친 이메일인지 여기서 확인한다.
        // 화면의 단계 진행에 맡기면 이 엔드포인트를 직접 호출해 인증을 건너뛸 수 있다.
        mailService.consumeVerifiedEmail(request.getEmail());

        loginRepository.save(User.createGeneralUser(
                request.getId(), request.getEmail(), passwordEncoder.encode(request.getPassword())));
    }

    /** 회원가입 화면의 중복 확인. 사용 가능하면 조용히 통과한다. */
    public void checkAvailable(String userId, String email) {
        if (loginRepository.existsByUserId(userId)) {
            throw new DuplicateAccountException(AccountMessage.EXISTS_ID);
        }

        if (loginRepository.existsByEmail(email)) {
            throw new DuplicateAccountException(AccountMessage.EXISTS_EMAIL);
        }
    }

    @Transactional
    public void login(LoginRequest request, HttpServletResponse response) {
        User user = findUserByUserId(request.getId());

        // 아이디가 없는 경우와 비밀번호가 틀린 경우를 구분해서 알려주지 않는다.
        if (!user.checkPassword(request.getPassword(), passwordEncoder)) {
            throw new LoginException(AccountMessage.NOT_FOUND_ACCOUNT);
        }

        user.updateLoginDate();

        Token token = jwtTokenProvider.createJwtToken(user.getUsername(), user.getRole());
        jwtService.login(token);
        cookieSupport.setJwtToken(token, response);
    }

    @Transactional
    public void logout(String userId, HttpServletResponse response) {
        if (userId != null) {
            jwtService.logout(userId);
        }

        cookieSupport.deleteJwtToken(response);
    }

    public UserResponse findUser(String userId) {
        return UserResponse.createResponse(findUserByUserId(userId));
    }

    public User findUserByUserId(String userId) {
        return loginRepository.findByUserId(userId)
                .orElseThrow(() -> new LoginException(AccountMessage.NOT_FOUND_ACCOUNT));
    }
}
