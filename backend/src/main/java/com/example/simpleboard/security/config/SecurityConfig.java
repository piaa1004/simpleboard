package com.example.simpleboard.security.config;

import com.example.simpleboard.common.exception.FilterExceptionHandler;
import com.example.simpleboard.common.response.ResponseCode;
import com.example.simpleboard.security.jwt.service.JwtService;
import com.example.simpleboard.security.jwt.support.CookieSupport;
import com.example.simpleboard.security.jwt.support.JwtAuthenticationFilter;
import com.example.simpleboard.security.jwt.support.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 세션을 쓰지 않는다. 인증 상태는 전적으로 쿠키에 담긴 JWT 가 결정한다.
 *
 * 읽기(GET)는 대부분 열어 두고, 쓰기(POST/PUT/PATCH/DELETE)는 로그인을 요구한다.
 *
 * 두 필터를 빈으로 두지 않고 여기서 직접 만드는 이유:
 * Filter 타입 빈은 스프링 부트가 서블릿 컨테이너에도 자동 등록해서, 시큐리티 체인과 별개로 한 번 더 실행된다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtService jwtService;
    private final CookieSupport cookieSupport;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // 컨트롤러가 예외를 던지면 서블릿이 ERROR 디스패치로 /error 를 다시 태운다.
                        // 이때는 인증 필터가 다시 돌지 않아 컨텍스트가 비어 있으므로,
                        // 열어두지 않으면 400 이어야 할 응답이 401 로 덮여버린다.
                        .requestMatchers("/error").permitAll()

                        // 회원가입 · 이메일 인증 · 로그인 · 로그아웃 · 아이디/이메일 중복 확인
                        .requestMatchers(HttpMethod.POST, "/registers", "/logins", "/user/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/mail/send", "/mail/check").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/exists").permitAll()

                        // 게시판 읽기는 비로그인도 가능하다. 조회수 증가도 마찬가지다.
                        .requestMatchers(HttpMethod.GET, "/posts", "/posts/**", "/comments/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/posts/views/**").permitAll()

                        .anyRequest().authenticated())

                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint((request, response, exception) ->
                                FilterExceptionHandler.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        ResponseCode.AUTHENTICATION_ERROR, "로그인이 필요한 서비스입니다."))
                        .accessDeniedHandler((request, response, exception) ->
                                FilterExceptionHandler.writeResponse(response, HttpServletResponse.SC_FORBIDDEN,
                                        ResponseCode.AUTHORIZATION_ERROR, "권한이 없습니다.")))

                // 먼저 등록한 쪽이 앞에 선다. 예외 변환 필터가 인증 필터보다 앞이어야 그 예외를 받을 수 있다.
                .addFilterBefore(new FilterExceptionHandler(cookieSupport), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, jwtService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
