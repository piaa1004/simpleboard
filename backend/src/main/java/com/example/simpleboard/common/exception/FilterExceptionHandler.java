package com.example.simpleboard.common.exception;

import com.example.simpleboard.common.response.ResponseCode;
import com.example.simpleboard.common.response.ResponseMessage;
import com.example.simpleboard.security.exception.TokenForgeryException;
import com.example.simpleboard.security.jwt.support.CookieSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @RestControllerAdvice 는 필터에서 던진 예외를 잡지 못한다.
 * 필터 체인 가장 앞에 서서 인증 관련 예외를 JSON 응답으로 바꾼다.
 */

@RequiredArgsConstructor
public class FilterExceptionHandler extends OncePerRequestFilter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CookieSupport cookieSupport;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (UsernameNotFoundException e) {
            // 토큰은 유효한데 그 사용자가 사라진 경우. 쿠키를 들고 있어봐야 계속 실패한다.
            cookieSupport.deleteJwtToken(response);
            writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    ResponseCode.AUTHENTICATION_ERROR, "토큰이 변조되었거나 유효하지 않습니다.");
        } catch (TokenForgeryException e) {
            cookieSupport.deleteJwtToken(response);
            writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    ResponseCode.AUTHENTICATION_ERROR, e.getMessage());
        }
    }

    public static void writeResponse(HttpServletResponse response, int status, ResponseCode code) throws IOException {
        write(response, status, ResponseMessage.of(code));
    }

    public static void writeResponse(HttpServletResponse response, int status, ResponseCode code, String message)
            throws IOException {
        write(response, status, ResponseMessage.of(code, message));
    }

    private static void write(HttpServletResponse response, int status, ResponseMessage<?> body) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(body));
    }
}
