package com.example.simpleboard.security.jwt.support;

import com.example.simpleboard.common.exception.FilterExceptionHandler;
import com.example.simpleboard.common.response.ResponseCode;
import com.example.simpleboard.security.jwt.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * 쿠키의 액세스 토큰으로 인증 컨텍스트를 채운다.
 *
 * 토큰이 만료됐으면 리프레시 토큰으로 새 액세스 토큰을 발급하고
 * 요청을 진행하는 대신 code 4 를 돌려준다. 프론트엔드가 그 신호를 보고 같은 요청을 한 번 더 보낸다.
 */

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        Optional<String> accessToken = CookieSupport.read(request, CookieSupport.ACCESS_TOKEN);

        if (accessToken.isEmpty()) {
            // 비로그인 사용자도 게시글을 읽을 수 있어야 하므로 그대로 통과시킨다.
            filterChain.doFilter(request, response);
            return;
        }

        if (jwtTokenProvider.validateAccessToken(accessToken.get())) {
            Authentication authentication = jwtTokenProvider.getAuthentication(accessToken.get());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
            return;
        }

        jwtService.reissueAccessToken(request, response);
        FilterExceptionHandler.writeResponse(response, HttpServletResponse.SC_OK, ResponseCode.CREATE_ACCESS_TOKEN);
    }
}
