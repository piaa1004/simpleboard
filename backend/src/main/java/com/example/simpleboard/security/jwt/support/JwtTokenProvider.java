package com.example.simpleboard.security.jwt.support;

import com.example.simpleboard.account.user.constant.UserRole;
import com.example.simpleboard.security.exception.TokenForgeryException;
import com.example.simpleboard.security.jwt.domain.RefreshToken;
import com.example.simpleboard.security.jwt.dto.Token;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * JWT 발급과 검증만 담당한다. 토큰을 쿠키에 싣는 일은 CookieSupport 가 한다.
 *
 * 액세스 토큰과 리프레시 토큰은 서로 다른 키로 서명한다.
 * 같은 키를 쓰면 액세스 토큰을 리프레시 토큰 자리에 넣어도 검증을 통과한다.
 */
@Component
public class JwtTokenProvider {

    private final UserDetailsService userDetailsService;
    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final Duration accessTokenValidity;
    private final Duration refreshTokenValidity;

    public JwtTokenProvider(UserDetailsService userDetailsService,
                            @Value("${app.jwt.access-secret}") String accessSecret,
                            @Value("${app.jwt.refresh-secret}") String refreshSecret,
                            @Value("${app.jwt.access-token-validity-seconds}") long accessSeconds,
                            @Value("${app.jwt.refresh-token-validity-seconds}") long refreshSeconds) {
        this.userDetailsService = userDetailsService;
        // HS256 은 256bit 이상의 키를 요구한다. 짧은 문자열을 넣으면 기동 시점에 바로 실패한다.
        this.accessKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidity = Duration.ofSeconds(accessSeconds);
        this.refreshTokenValidity = Duration.ofSeconds(refreshSeconds);
    }

    public Token createJwtToken(String userId, UserRole role) {
        return Token.builder()
                .accessToken(createToken(userId, role, accessKey, accessTokenValidity))
                .refreshToken(createToken(userId, role, refreshKey, refreshTokenValidity))
                .key(userId)
                .build();
    }

    /** 리프레시 토큰이 아직 살아 있으면 새 액세스 토큰을 만들어 준다. 만료됐으면 null. */
    public String recreateAccessToken(RefreshToken refreshToken) {
        try {
            Claims claims = parse(refreshToken.getToken(), refreshKey);

            return createToken(claims.getSubject(), UserRole.valueOf(claims.get("role", String.class)),
                    accessKey, accessTokenValidity);
        } catch (ExpiredJwtException e) {
            return null;
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenForgeryException("변조된 RefreshToken 입니다.");
        }
    }

    public Authentication getAuthentication(String accessToken) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getUserId(accessToken));

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /** 액세스 토큰에서 사용자 아이디를 꺼낸다. */
    public String getUserId(String accessToken) {
        return parse(accessToken, accessKey).getSubject();
    }

    public boolean validateAccessToken(String accessToken) {
        try {
            parse(accessToken, accessKey);
            return true;
        } catch (ExpiredJwtException e) {
            // 만료는 정상적인 흐름이다. 호출자가 리프레시 토큰으로 재발급을 시도한다.
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenForgeryException("알 수 없는 토큰이거나 변조되었습니다.");
        }
    }

    private String createToken(String userId, UserRole role, SecretKey key, Duration validity) {
        Date now = new Date();

        return Jwts.builder()
                .subject(userId)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + validity.toMillis()))
                .signWith(key)
                .compact();
    }

    private Claims parse(String token, SecretKey key) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
