package com.example.simpleboard.security.jwt.service;

import com.example.simpleboard.security.exception.TokenForgeryException;
import com.example.simpleboard.security.jwt.domain.RefreshToken;
import com.example.simpleboard.security.jwt.dto.Token;
import com.example.simpleboard.security.jwt.repository.RefreshTokenRepository;
import com.example.simpleboard.security.jwt.support.CookieSupport;
import com.example.simpleboard.security.jwt.support.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieSupport cookieSupport;

    /** 로그인할 때마다 그 사용자의 리프레시 토큰을 새 것 하나로 갈아 끼운다. */
    @Transactional
    public void login(Token token) {
        refreshTokenRepository.deleteByKeyId(token.getKey());
        refreshTokenRepository.save(RefreshToken.createRefreshToken(token));
    }

    @Transactional
    public void logout(String userId) {
        refreshTokenRepository.deleteByKeyId(userId);
    }

    /**
     * 액세스 토큰이 만료됐을 때 호출된다.
     * 쿠키의 리프레시 토큰이 DB 에 저장된 것과 같고 아직 살아 있으면 새 액세스 토큰을 쿠키에 심는다.
     */
    @Transactional(readOnly = true)
    public void reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieSupport.read(request, CookieSupport.REFRESH_TOKEN)
                .orElseThrow(() -> new TokenForgeryException("인증되지 않은 사용자입니다."));

        RefreshToken saved = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new TokenForgeryException("알 수 없는 RefreshToken 입니다."));

        String accessToken = jwtTokenProvider.recreateAccessToken(saved);
        if (accessToken == null) {
            throw new TokenForgeryException("만료된 RefreshToken 입니다. 다시 로그인해주세요.");
        }

        cookieSupport.setAccessToken(accessToken, response);
    }
}
