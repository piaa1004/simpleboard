package com.example.simpleboard.security.jwt.support;

import com.example.simpleboard.security.jwt.dto.Token;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

/**
 * JWT 는 HttpOnly 쿠키로만 주고받는다. 자바스크립트가 토큰을 읽지 못하게 하려는 것이다.
 *
 * 원본 프로젝트는 이 클래스가 static 유틸이면서 static 필드에 @Value 를 걸어
 * 도메인 값이 항상 null 이었다. 여기서는 평범한 빈으로 만들어 설정이 실제로 반영되게 한다.
 */
@Component
public class CookieSupport {

    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";

    private final boolean secure;
    private final String sameSite;
    private final String domain;
    private final Duration accessTokenMaxAge;
    private final Duration refreshTokenMaxAge;

    public CookieSupport(@Value("${app.cookie.secure}") boolean secure,
                         @Value("${app.cookie.same-site}") String sameSite,
                         @Value("${app.cookie.domain:}") String domain,
                         @Value("${app.jwt.access-token-validity-seconds}") long accessSeconds,
                         @Value("${app.jwt.refresh-token-validity-seconds}") long refreshSeconds) {
        this.secure = secure;
        this.sameSite = sameSite;
        this.domain = domain;
        this.accessTokenMaxAge = Duration.ofSeconds(accessSeconds);
        this.refreshTokenMaxAge = Duration.ofSeconds(refreshSeconds);
    }

    public void setJwtToken(Token token, HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, build(ACCESS_TOKEN, token.getAccessToken(), accessTokenMaxAge).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, build(REFRESH_TOKEN, token.getRefreshToken(), refreshTokenMaxAge).toString());
    }

    public void setAccessToken(String accessToken, HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, build(ACCESS_TOKEN, accessToken, accessTokenMaxAge).toString());
    }

    public void deleteJwtToken(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, build(ACCESS_TOKEN, "", Duration.ZERO).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, build(REFRESH_TOKEN, "", Duration.ZERO).toString());

        SecurityContextHolder.clearContext();
    }

    /** 요청 쿠키에서 값을 꺼낸다. 없으면 비어 있는 Optional. */
    public static Optional<String> read(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(StringUtils::hasText)
                .findFirst();
    }

    private ResponseCookie build(String name, String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .path("/")
                .maxAge(maxAge)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite);

        // 도메인을 비워두면 요청을 받은 호스트에만 붙는다. 로컬 개발에서는 이쪽이 맞다.
        if (StringUtils.hasText(domain)) {
            builder.domain(domain);
        }

        return builder.build();
    }
}
